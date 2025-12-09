package com.example;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lógica de negocio central que implementa las reglas del PARCIAL.
 */
public class EPSService {
    private final EPS eps;
    // lock por doctor para evitar race conditions al agendar
    private final Map<UUID, Object> locks = new HashMap<>();

    public EPSService(EPS eps) {
        this.eps = eps;
    }

    private Object lockForDoctor(UUID doctorId) {
        return locks.computeIfAbsent(doctorId, id -> new Object());
    }

    // RF1 - RF2
    public void registrarPaciente(Paciente p) {
        // validaciones básicas
        if (p.getIdentificacion() == null || p.getNombre() == null) throw new IllegalArgumentException("Datos obligatorios paciente");
        eps.registrarPaciente(p);
    }

    public void registrarDoctor(Doctor d) {
        if (d.getIdentificacion() == null || d.getNombre() == null || d.getEspecialidad() == null)
            throw new IllegalArgumentException("Datos obligatorios doctor");
        eps.registrarDoctor(d);
    }

    // RF3 - Crear Cita en estado PENDIENTE
    public Optional<Cita> crearCita(UUID pacienteId, UUID doctorId, LocalDateTime fechaHora, Duration duracion, String motivo) {
        // validaciones
        if (fechaHora.isBefore(LocalDateTime.now())) return Optional.empty(); // fecha debe ser futura
        Optional<Paciente> pac = eps.obtenerPaciente(pacienteId);
        Optional<Doctor> doc = eps.obtenerDoctor(doctorId);
        if (!pac.isPresent() || !doc.isPresent()) return Optional.empty();

        Cita nueva = new Cita(pacienteId, doctorId, fechaHora, duracion, motivo);

        synchronized (lockForDoctor(doctorId)) {
            // 1) doctor disponible en ese horario?
            boolean dentroHorario = doc.get().getHorariosDelDia(fechaHora.getDayOfWeek()).stream()
                    .anyMatch(h -> h.contiene(fechaHora.toLocalTime(), fechaHora.toLocalTime().plus(duracion)));
            if (!dentroHorario) return Optional.empty();

            // 2) paciente no tenga cita con mismo doctor en mismo momento
            boolean duplicada = eps.listarCitas().stream()
                    .filter(c -> c.getDoctorId().equals(doctorId))
                    .filter(c -> c.getPacienteId().equals(pacienteId))
                    .anyMatch(c -> c.getFechaHora().isEqual(fechaHora) && c.getEstado() != Cita.Estado.CANCELADA);
            if (duplicada) return Optional.empty();

            // 3) conflicto con otras citas del doctor
            boolean solapa = eps.listarCitas().stream()
                    .filter(c -> c.getDoctorId().equals(doctorId))
                    .filter(c -> c.getEstado() != Cita.Estado.CANCELADA)
                    .anyMatch(c -> c.solapaCon(nueva));
            if (solapa) return Optional.empty();

            // guardar
            eps.guardarCita(nueva);
            return Optional.of(nueva);
        }
    }
    // ---------- VALIDADOR DETALLADO PARA CREAR CITA ----------
public String validarCrearCita(UUID pacienteId, UUID doctorId, LocalDateTime fechaHora, Duration duracion) {

    if (fechaHora.isBefore(LocalDateTime.now()))
        return "La fecha debe ser futura.";

    var pac = eps.obtenerPaciente(pacienteId);
    var doc = eps.obtenerDoctor(doctorId);

    if (!pac.isPresent())
        return "El paciente no existe.";
    if (!doc.isPresent())
        return "El doctor no existe.";

    // Validar horario disponible
    boolean disponible = doc.get().getHorariosDelDia(fechaHora.getDayOfWeek()).stream()
            .anyMatch(h -> h.contiene(
                    fechaHora.toLocalTime(),
                    fechaHora.toLocalTime().plus(duracion)
            ));

    if (!disponible)
        return "El doctor NO trabaja en ese horario.";

    // Validar cita duplicada
    boolean duplicada = eps.listarCitas().stream()
            .filter(c -> c.getDoctorId().equals(doctorId))
            .filter(c -> c.getPacienteId().equals(pacienteId))
            .anyMatch(c -> c.getFechaHora().isEqual(fechaHora) &&
                    c.getEstado() != Cita.Estado.CANCELADA);

    if (duplicada)
        return "El paciente ya tiene una cita a esa hora con ese doctor.";

    // Validar solapamiento
    Cita tentativa = new Cita(pacienteId, doctorId, fechaHora, duracion, "tmp");

    boolean solapa = eps.listarCitas().stream()
            .filter(c -> c.getDoctorId().equals(doctorId))
            .filter(c -> c.getEstado() != Cita.Estado.CANCELADA)
            .anyMatch(c -> c.solapaCon(tentativa));

    if (solapa)
        return "El doctor ya tiene otra cita en ese horario.";

    return "OK";
}


    // RF4 - Modificar cita solo si PENDIENTE
    public boolean modificarCita(UUID citaId, LocalDateTime nuevaFecha, Duration nuevaDuracion, String nuevoMotivo) {
        Optional<Cita> opt = eps.obtenerCita(citaId);
        if (!opt.isPresent()) return false;
        Cita c = opt.get();
        if (c.getEstado() != Cita.Estado.PENDIENTE) return false;
        if (nuevaFecha.isBefore(LocalDateTime.now())) return false;

        synchronized (lockForDoctor(c.getDoctorId())) {
            // chequear disponibilidad doctor y conflictos (excluyendo la propia cita)
            Optional<Doctor> doc = eps.obtenerDoctor(c.getDoctorId());
            if (!doc.isPresent()) return false;

            boolean dentroHorario = doc.get().getHorariosDelDia(nuevaFecha.getDayOfWeek()).stream()
                    .anyMatch(h -> h.contiene(nuevaFecha.toLocalTime(), nuevaFecha.toLocalTime().plus(nuevaDuracion)));
            if (!dentroHorario) return false;

            boolean conflicto = eps.listarCitas().stream()
                    .filter(other -> !other.getId().equals(citaId))
                    .filter(other -> other.getDoctorId().equals(c.getDoctorId()))
                    .filter(other -> other.getEstado() != Cita.Estado.CANCELADA)
                    .anyMatch(other -> {
                        Cita temp = new Cita(c.getPacienteId(), c.getDoctorId(), nuevaFecha, nuevaDuracion, nuevoMotivo);
                        return other.solapaCon(temp);
                    });
            if (conflicto) return false;

            // aplicar cambios
            c.setFechaHora(nuevaFecha);
            c.setDuracion(nuevaDuracion);
            c.setMotivo(nuevoMotivo);
            eps.guardarCita(c);
            return true;
        }
    }

    // RF5 - Cancelar
    public boolean cancelarCita(UUID citaId) {
        Optional<Cita> opt = eps.obtenerCita(citaId);
        if (!opt.isPresent()) return false;
        Cita c = opt.get();
        if (c.getEstado() == Cita.Estado.CANCELADA) return false;
        c.setEstado(Cita.Estado.CANCELADA);
        eps.guardarCita(c);
        return true;
    }

    // RF6-RF9 - Atender cita: cambia a ATENDIDA y genera entrada en historia clínica, registra medicamentos descontando stock
    public boolean atenderCita(UUID citaId, String diagnostico, String tratamiento, List<HistoriaClinica.MedicamentoPrescrito> medsPrescritos) {
        Optional<Cita> opt = eps.obtenerCita(citaId);
        if (!opt.isPresent()) return false;
        Cita c = opt.get();
        if (c.getEstado() != Cita.Estado.PENDIENTE) return false;

        // validar doctor disponible en ese horario (regla de negocio)
        Optional<Doctor> docOpt = eps.obtenerDoctor(c.getDoctorId());
        if (!docOpt.isPresent()) return false;
        boolean dentroHorario = docOpt.get().getHorariosDelDia(c.getFechaHora().getDayOfWeek()).stream()
                .anyMatch(h -> h.contiene(c.getFechaHora().toLocalTime(), c.getFin().toLocalTime()));
        if (!dentroHorario) return false;

        // validar stock de medicamentos
        if (medsPrescritos != null) {
            for (HistoriaClinica.MedicamentoPrescrito mp : medsPrescritos) {
                Optional<Medicamento> mOpt = eps.obtenerMedicamento(mp.getMedicamentoId());
                if (!mOpt.isPresent()) return false;
                Medicamento m = mOpt.get();
                if (m.getCantidadDisponible() <= 0 || mp.getCantidad() <= 0 || mp.getCantidad() > m.getCantidadDisponible()) return false;
            }
        }

        // descontar stock y registrar en historia
        if (medsPrescritos != null) {
            for (HistoriaClinica.MedicamentoPrescrito mp : medsPrescritos) {
                Medicamento m = eps.obtenerMedicamento(mp.getMedicamentoId()).get();
                m.dispensar(mp.getCantidad());
                eps.guardarMedicamento(m);
            }
        }

        // marcar cita como atendida
        c.setEstado(Cita.Estado.ATENDIDA);
        eps.guardarCita(c);

        // crear entrada en historia clínica (no modificable luego)
        HistoriaClinica historia = eps.obtenerHistoria(c.getPacienteId());
        historia.registrarConsulta(LocalDateTime.now(), c.getDoctorId(), diagnostico, tratamiento, 
                medsPrescritos == null ? Collections.emptyList() : medsPrescritos.stream().map(mp -> new HistoriaClinica.MedicamentoPrescrito(mp.getMedicamentoId(), mp.getCantidad())).collect(Collectors.toList()));
        return true;
    }

    // RF8 - gestionar inventario
    public void agregarMedicamento(Medicamento m) { eps.guardarMedicamento(m); }
    public boolean actualizarStockMedicamento(UUID medicamentoId, int nuevaCantidad) {
        Optional<Medicamento> mOpt = eps.obtenerMedicamento(medicamentoId);
        if (!mOpt.isPresent()) return false;
        Medicamento m = mOpt.get();
        m.actualizarStock(nuevaCantidad);
        eps.guardarMedicamento(m);
        return true;
    }

    // RF10 - consultar historia clínica
    public Optional<HistoriaClinica> consultarHistoria(UUID pacienteId) {
        return Optional.ofNullable(eps.obtenerHistoria(pacienteId));
    }

    // RF11 - listar
    public Collection<Paciente> listarPacientes() { return eps.listarPacientes(); }
    public Collection<Doctor> listarDoctores() { return eps.listarDoctores(); }
    public Collection<Cita> listarCitas() { return eps.listarCitas(); }
    public Collection<Medicamento> listarMedicamentos() { return eps.listarMedicamentos(); }

    public List<String> obtenerDisponibilidad(UUID doctorId, LocalDate fecha, Duration duracion) {
    Optional<Doctor> docOpt = eps.obtenerDoctor(doctorId);
    if (!docOpt.isPresent()) return List.of("Doctor no encontrado");

    Doctor doc = docOpt.get();
    List<Doctor.Horario> horarios = doc.getHorariosDelDia(fecha.getDayOfWeek());

    if (horarios.isEmpty()) return List.of("El doctor NO trabaja este día");

    // citas del doctor ese día
    List<Cita> citas = eps.listarCitas().stream()
            .filter(c -> c.getDoctorId().equals(doctorId))
            .filter(c -> c.getFechaHora().toLocalDate().equals(fecha))
            .filter(c -> c.getEstado() != Cita.Estado.CANCELADA)
            .sorted(Comparator.comparing(Cita::getFechaHora))
            .toList();

    List<String> disponibles = new ArrayList<>();

    for (Doctor.Horario h : horarios) {
        LocalTime inicio = h.getDesde();
        LocalTime fin = h.getHasta();

        for (Cita c : citas) {
            LocalTime cIni = c.getFechaHora().toLocalTime();
            LocalTime cFin = c.getFin().toLocalTime();

            if (Duration.between(inicio, cIni).toMinutes() >= duracion.toMinutes())
                disponibles.add(inicio + " - " + cIni);

            inicio = cFin;
        }

        if (Duration.between(inicio, fin).toMinutes() >= duracion.toMinutes())
            disponibles.add(inicio + " - " + fin);
    }

    if (disponibles.isEmpty())
        disponibles.add("No hay espacio disponible");

    return disponibles;
}

}