package com.example;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    @SuppressWarnings("resource")
    private static final Scanner sc = new Scanner(System.in);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        EPS eps = new EPS("MiEPS");
        EPSService service = new EPSService(eps);
        int opcion;
        do {
            System.out.println("\n==============================================");
            System.out.println("            SISTEMA EPS - MENÚ PRINCIPAL      ");
            System.out.println("==============================================");
            System.out.println(" 1) Registrar Paciente");
            System.out.println(" 2) Registrar Doctor");
            System.out.println(" 3) Crear Cita");
            System.out.println(" 4) Modificar Cita");
            System.out.println(" 5) Cancelar Cita");
            System.out.println(" 6) Atender Cita");
            System.out.println(" 7) Consultar Historia Clínica");
            System.out.println(" 8) Listar Pacientes");
            System.out.println(" 9) Listar Doctores");
            System.out.println(" 0) Salir");
            System.out.print("----------------------------------------------\nSeleccione una opción: ");
            opcion = leerEntero();
            switch (opcion) {
                case 1 -> registrarPaciente(service);
                case 2 -> registrarDoctor(service);
                case 3 -> crearCita(service);
                case 4 -> modificarCita(service);
                case 5 -> cancelarCita(service);
                case 6 -> atenderCita(service);
                case 7 -> consultarHistoria(service);
                case 8 -> listarPacientes(service);
                case 9 -> listarDoctores(service);
                case 0 -> System.out.println("Saliendo...");
                default -> System.out.println("❌ Opción inválida.");
            }
        } while (opcion != 0);
    }

    // ------------------ UTILIDADES ------------------
    private static int leerEntero() {
        while (true) {
            try {
                String line = sc.nextLine().trim();
                return Integer.parseInt(line);
            } catch (Exception e) {
                System.out.print("Entrada inválida. Intente nuevamente: ");
            }
        }
    }

    private static LocalDate leerFecha() {
        while (true) {
            try {
                System.out.print("Fecha (YYYY-MM-DD): ");
                String s = sc.nextLine().trim();
                return LocalDate.parse(s);
            } catch (Exception e) {
                System.out.println("❌ Fecha incorrecta. Ejemplo válido: 2025-12-10");
            }
        }
    }

    private static LocalTime leerHoraFull(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + " (HH:MM): ");
                String s = sc.nextLine().trim();
                return LocalTime.parse(s, TIME_FMT);
            } catch (Exception e) {
                System.out.println("❌ Hora incorrecta. Ejemplo válido: 14:30");
            }
        }
    }

    private static LocalTime leerHora() {
        return leerHoraFull("Hora");
    }

    // ------------------ MENÚS ------------------

    private static void registrarPaciente(EPSService service) {
        System.out.println("\n=== REGISTRO DE PACIENTE ===");
        System.out.print("Identificación: ");
        String id = sc.nextLine();
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();
        System.out.print("Edad: ");
        int edad = leerEntero();
        System.out.print("Dirección: ");
        String dir = sc.nextLine();
        System.out.print("Teléfono: ");
        String tel = sc.nextLine();
        System.out.print("EPS asignada: ");
        String epsTxt = sc.nextLine();
        Paciente p = new Paciente(id, nombre, edad, dir, tel, epsTxt);
        service.registrarPaciente(p);
        System.out.println("✔ Paciente registrado. Identificación interna: " + p.getId());
    }

    /**
     * Registrar doctor: permite introducir varios días separados por comas,
     * y añade el mismo intervalo horario para cada día.
     */
    private static void registrarDoctor(EPSService service) {
        System.out.println("\n=== REGISTRO DE DOCTOR ===");
        System.out.print("Identificación: ");
        String id = sc.nextLine();
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();
        System.out.print("Especialidad: ");
        String esp = sc.nextLine();
        Doctor d = new Doctor(id, nombre, esp);

        System.out.println("\n--- HORARIO DEL DOCTOR ---");
        System.out.print("Días (ej: 1,2,3 para Lun,Mar,Mié) o un solo número: ");
        String diasLine = sc.nextLine().trim();
        List<Integer> dias = parseListaEnteros(diasLine);
        if (dias.isEmpty()) {
            System.out.println("❌ No se ingresaron días válidos. Se canceló el registro de horario.");
        } else {
            LocalTime inicio = leerHoraFull("Hora inicio");
            LocalTime fin = leerHoraFull("Hora fin");
            if (!fin.isAfter(inicio)) {
                System.out.println("❌ Hora fin debe ser posterior a hora inicio. No se agregó horario.");
            } else {
                for (int dia : dias) {
                    try {
                        d.agregarHorario(DayOfWeek.of(dia), inicio, fin);
                    } catch (Exception e) {
                        System.out.println("⚠ Día inválido: " + dia + " (ignorando).");
                    }
                }
                System.out.println("✔ Horario agregado para días: "
                        + dias.stream().map(Object::toString).collect(Collectors.joining(",")));
            }
        }
        service.registrarDoctor(d);
        System.out.println("✔ Doctor registrado: " + d.getIdentificacion() + " - " + d.getNombre());
    }

    private static List<Integer> parseListaEnteros(String s) {
        if (s == null || s.isBlank())
            return Collections.emptyList();
        String[] parts = s.split(",");
        List<Integer> res = new ArrayList<>();
        for (String p : parts) {
            try {
                res.add(Integer.parseInt(p.trim()));
            } catch (Exception ignored) {
            }
        }
        return res;
    }

    /**
     * Crear cita: listado numerado de pacientes y doctores; muestra disponibilidad
     * del doctor para la fecha y duración solicitada antes de intentar crearla.
     */
    private static void crearCita(EPSService service) {
        System.out.println("\n=== CREAR CITA ===");
        List<Paciente> pacientes = new ArrayList<>(service.listarPacientes());
        if (pacientes.isEmpty()) {
            System.out.println("❌ No hay pacientes.");
            return;
        }
        System.out.println("\n--- PACIENTES ---");
        for (int i = 0; i < pacientes.size(); i++)
            System.out.println((i + 1) + ") " + pacienteResumen(pacientes.get(i)));
        System.out.print("Seleccione paciente: ");
        int idxP = leerEntero() - 1;
        if (idxP < 0 || idxP >= pacientes.size()) {
            System.out.println("❌ Selección inválida.");
            return;
        }
        Paciente paciente = pacientes.get(idxP);

        List<Doctor> doctores = new ArrayList<>(service.listarDoctores());
        if (doctores.isEmpty()) {
            System.out.println("❌ No hay doctores.");
            return;
        }
        System.out.println("\n--- DOCTORES ---");
        for (int i = 0; i < doctores.size(); i++)
            System.out.println((i + 1) + ") " + doctorResumen(doctores.get(i)));
        System.out.print("Seleccione doctor: ");
        int idxD = leerEntero() - 1;
        if (idxD < 0 || idxD >= doctores.size()) {
            System.out.println("❌ Selección inválida.");
            return;
        }
        Doctor doctor = doctores.get(idxD);

        LocalDate fecha = leerFecha();
        System.out.print("Duración estimada (min): ");
        int dur = leerEntero();
        // Antes de pedir hora, mostrar disponibilidad basada en duración
        List<String> slots = service.obtenerDisponibilidad(doctor.getId(), fecha, Duration.ofMinutes(dur));
        System.out.println("\n--- HORARIOS DISPONIBLES EL " + fecha + " (duración " + dur + " min) ---");
        for (int i = 0; i < slots.size(); i++)
            System.out.println((i + 1) + ") " + slots.get(i));
        System.out.println("0) Ingresar hora manualmente");
        System.out.print("Seleccione opción para elegir la franja (número): ");
        int sel = leerEntero();
        LocalTime hora;
        if (sel == 0) {
            hora = leerHoraFull("Hora (manual)");
        } else {
            if (sel < 1 || sel > slots.size()) {
                System.out.println("❌ Selección inválida.");
                return;
            }
            String franja = slots.get(sel - 1);
            if (franja.equals("No hay espacio disponible") || franja.equals("El doctor NO trabaja este día")
                    || franja.equals("Doctor no encontrado")) {
                System.out.println("❌ " + franja);
                return;
            }
            // franja ejemplo "07:00 - 08:00" o "07:00 - 07:30"
            String[] parts = franja.split("-");
            hora = LocalTime.parse(parts[0].trim(), TIME_FMT);
        }

        LocalDateTime fechaHora = fecha.atTime(hora);
        System.out.print("Motivo: ");
        String motivo = sc.nextLine();

        // VALIDACIÓN DETALLADA
        String validar = service.validarCrearCita(paciente.getId(), doctor.getId(), fechaHora, Duration.ofMinutes(dur));
        if (!validar.equals("OK")) {
            System.out.println("❌ " + validar);
            return;
        }
        var citaOpt = service.crearCita(paciente.getId(), doctor.getId(), fechaHora, Duration.ofMinutes(dur), motivo);
        if (citaOpt.isPresent()) {
            System.out.println("✔ Cita creada: " + resumenCitaLegible(citaOpt.get(), paciente, doctor));
        } else {
            System.out.println("❌ No se pudo crear la cita (validación inesperada).");
        }
    }

    private static void modificarCita(EPSService service) {
        System.out.println("\n=== MODIFICAR CITA ===");
        List<Cita> citas = new ArrayList<>(service.listarCitas());
        if (citas.isEmpty()) {
            System.out.println("❌ No hay citas.");
            return;
        }
        Map<UUID, Paciente> pacienteMap = service.listarPacientes().stream()
                .collect(Collectors.toMap(Paciente::getId, p -> p));
        Map<UUID, Doctor> doctorMap = service.listarDoctores().stream()
                .collect(Collectors.toMap(Doctor::getId, d -> d));
        for (int i = 0; i < citas.size(); i++) {
            Cita c = citas.get(i);
            System.out.println((i + 1) + ") "
                    + resumenCitaLegible(c, pacienteMap.get(c.getPacienteId()), doctorMap.get(c.getDoctorId())));
        }
        System.out.print("Seleccione cita: ");
        int idx = leerEntero() - 1;
        if (idx < 0 || idx >= citas.size())
            return;
        Cita c = citas.get(idx);

        LocalDate f = leerFecha();
        LocalTime h = leerHora();
        System.out.print("Duración (min): ");
        int dur = leerEntero();
        System.out.print("Motivo: ");
        String mot = sc.nextLine();

        boolean ok = service.modificarCita(c.getId(), f.atTime(h), Duration.ofMinutes(dur), mot);
        System.out.println(ok ? "✔ Cita modificada." : "❌ No se pudo modificar.");
    }

    private static void cancelarCita(EPSService service) {
        System.out.println("\n=== CANCELAR CITA ===");
        List<Cita> citas = new ArrayList<>(service.listarCitas());
        if (citas.isEmpty()) {
            System.out.println("❌ No hay citas.");
            return;
        }
        Map<UUID, Paciente> pacienteMap = service.listarPacientes().stream()
                .collect(Collectors.toMap(Paciente::getId, p -> p));
        Map<UUID, Doctor> doctorMap = service.listarDoctores().stream()
                .collect(Collectors.toMap(Doctor::getId, d -> d));
        for (int i = 0; i < citas.size(); i++)
            System.out.println((i + 1) + ") " + resumenCitaLegible(citas.get(i),
                    pacienteMap.get(citas.get(i).getPacienteId()), doctorMap.get(citas.get(i).getDoctorId())));
        System.out.print("Seleccione cita: ");
        int idx = leerEntero() - 1;
        if (idx < 0 || idx >= citas.size())
            return;
        boolean ok = service.cancelarCita(citas.get(idx).getId());
        System.out.println(ok ? "✔ Cita cancelada." : "❌ No se pudo cancelar.");
    }

    private static void atenderCita(EPSService service) {
        System.out.println("\n=== ATENDER CITA ===");
        List<Cita> citas = new ArrayList<>(service.listarCitas()).stream()
                .filter(c -> c.getEstado() == Cita.Estado.PENDIENTE)
                .collect(Collectors.toList());
        if (citas.isEmpty()) {
            System.out.println("❌ No hay citas pendientes.");
            return;
        }
        Map<UUID, Paciente> pacienteMap = service.listarPacientes().stream()
                .collect(Collectors.toMap(Paciente::getId, p -> p));
        Map<UUID, Doctor> doctorMap = service.listarDoctores().stream()
                .collect(Collectors.toMap(Doctor::getId, d -> d));
        for (int i = 0; i < citas.size(); i++)
            System.out.println((i + 1) + ") " + resumenCitaLegible(citas.get(i),
                    pacienteMap.get(citas.get(i).getPacienteId()), doctorMap.get(citas.get(i).getDoctorId())));
        System.out.print("Seleccione cita: ");
        int idx = leerEntero() - 1;
        if (idx < 0 || idx >= citas.size())
            return;
        Cita c = citas.get(idx);
        System.out.print("Diagnóstico: ");
        String diag = sc.nextLine();
        System.out.print("Tratamiento: ");
        String trat = sc.nextLine();
        boolean ok = service.atenderCita(c.getId(), diag, trat, new ArrayList<>());
        System.out.println(ok ? "✔ Cita atendida." : "❌ No fue posible atenderla.");
    }

    private static void consultarHistoria(EPSService service) {
        System.out.println("\n=== CONSULTA HISTORIA CLÍNICA ===");
        List<Paciente> pacientes = new ArrayList<>(service.listarPacientes());
        if (pacientes.isEmpty()) {
            System.out.println("❌ No hay pacientes.");
            return;
        }
        for (int i = 0; i < pacientes.size(); i++)
            System.out.println((i + 1) + ") " + pacienteResumen(pacientes.get(i)));
        System.out.print("Seleccione paciente: ");
        int idx = leerEntero() - 1;
        if (idx < 0 || idx >= pacientes.size())
            return;
        Paciente paciente = pacientes.get(idx);
        var histOpt = service.consultarHistoria(paciente.getId());
        if (histOpt.isEmpty() || histOpt.get().verHistorial().isEmpty()) {
            System.out.println("❌ No tiene historia clínica.");
            return;
        }
        List<HistoriaClinica.Consulta> consultas = histOpt.get().verHistorial();
        Map<UUID, Doctor> doctorMap = service.listarDoctores().stream()
                .collect(Collectors.toMap(Doctor::getId, d -> d));
        System.out.println("\n--- HISTORIA CLÍNICA DE " + paciente.getNombre() + " ---");
        for (int i = 0; i < consultas.size(); i++) {
            HistoriaClinica.Consulta c = consultas.get(i);
            Doctor doc = doctorMap.get(c.getDoctorId());
            String docInfo = doc == null ? c.getDoctorId().toString() : doctorResumen(doc);
            System.out.println("\nConsulta #" + (i + 1));
            System.out.println(" Fecha:      " + c.getFecha().format(DATE_TIME_FMT));
            System.out.println(" Doctor:     " + docInfo);
            System.out.println(" Diagnóstico:" + " " + c.getDiagnostico());
            System.out.println(" Tratamiento:" + " " + c.getTratamiento());
            System.out.println(" Medicamentos:");
            if (c.getMedicamentos().isEmpty()) {
                System.out.println("  - (ninguno)");
            } else {
                for (HistoriaClinica.MedicamentoPrescrito mp : c.getMedicamentos())
                    System.out.println("  - " + mp);
            }
        }
    }

    private static void listarPacientes(EPSService service) {
        System.out.println("\n=== LISTA DE PACIENTES ===");
        List<Paciente> list = new ArrayList<>(service.listarPacientes());
        if (list.isEmpty())
            System.out.println("❌ No hay pacientes.");
        else {
            for (int i = 0; i < list.size(); i++)
                System.out.println((i + 1) + ") " + pacienteResumen(list.get(i)));
        }
    }

    private static void listarDoctores(EPSService service) {
        System.out.println("\n=== LISTA DE DOCTORES ===");
        List<Doctor> list = new ArrayList<>(service.listarDoctores());
        if (list.isEmpty())
            System.out.println("❌ No hay doctores.");
        else {
            for (int i = 0; i < list.size(); i++) {
                Doctor d = list.get(i);
                System.out.println((i + 1) + ") " + doctorResumen(d));
                // mostrar horarios brevemente
                // (omitir detalle aquí para no saturar)
            }
        }
    }

    // ------------------ AYUDAS DE FORMATO ------------------

    private static String pacienteResumen(Paciente p) {
        return String.format("%s — %s (%d años)", p.getIdentificacion(), p.getNombre(), p.getEdad());
    }

    private static String doctorResumen(Doctor d) {
        return String.format("%s — %s (%s)", d.getIdentificacion(), d.getNombre(), d.getEspecialidad());
    }

    private static String resumenCitaLegible(Cita c, Paciente p, Doctor d) {
        String pacienteTxt = (p == null) ? c.getPacienteId().toString()
                : (p.getIdentificacion() + " - " + p.getNombre());
        String doctorTxt = (d == null) ? c.getDoctorId().toString() : (d.getIdentificacion() + " - " + d.getNombre());
        return String.format("%s | %s -> %s | %s | %s",
                c.getFechaHora().format(DATE_TIME_FMT),
                doctorTxt,
                pacienteTxt,
                c.getDuracion().toMinutes() + "min",
                c.getEstado().name());
    }
}
