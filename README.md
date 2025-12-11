Programación II – Proyecto EPS

Proyecto de grupo de Programación II
Backend para la gestión de una Entidad Promotora de Salud (EPS).

------------------------------------------------------------------------

DESCRIPCIÓN GENERAL DEL PROYECTO

El proyecto consiste en un sistema backend para administrar: - Pacientes
y sus datos personales - Médicos y sus especialidades - Citas médicas -
Historia clínica - Medicamentos

------------------------------------------------------------------------

PROPÓSITO DEL PROYECTO - Digitalizar la EPS - Registrar/consultar
pacientes, médicos y citas - Control de citas - Bases para módulos
futuros

------------------------------------------------------------------------

OBJETOS PRINCIPALES

Paciente: - Atributos: identificación, nombre, edad, dirección,
teléfono, EPS. - Métodos: actualizar datos, consultar, solicitar cita,
ver historial.

Doctor: - Atributos: identificación, nombre, especialidad, horarios. -
Métodos: atender, registrar diagnóstico, recetar.

Cita Médica: - Atributos: paciente, doctor, fecha, hora, motivo,
estado. - Métodos: asignar, cancelar, consultar, reprogramar.

Medicamento: - Atributos: nombre, dosis, stock, vencimiento. - Métodos:
dispensar, consultar stock, actualizar inventario.

Historia Clínica: - Atributos: paciente, consultas, diagnósticos,
tratamientos. - Métodos: registrar consulta, ver historial.

EPS: - Atributos: nombre, pacientes, doctores. - Métodos: registrar
paciente, contratar doctor.

------------------------------------------------------------------------

REQUERIMIENTOS FUNCIONALES 
RF1 Registrar Paciente 
RF2 Registrar Doctor
RF3 Crear Cita 
RF4 Modificar Cita 
RF5 Cancelar Cita 
RF6 Atender Cita 
RF7 Registrar diagnóstico 
RF8 Gestionar inventario 
RF9 Registrar medicamentos formulados 
RF10 Consultar historia clínica 
RF11 Listar pacientes y doctores

------------------------------------------------------------------------

REGLAS DEL NEGOCIO 
- Cita: PENDIENTE editable, ATENDIDA/CANCELADA no
editable 
- No citas duplicadas 
- Historia clínica se genera al atender 
-Medicamentos requieren stock 
- Validaciones de fechas, datos,
disponibilidad

------------------------------------------------------------------------

CASOS DE ACEPTACIÓN 
CA1 Verificar disponibilidad 
CA2 Edición solo si
PENDIENTE 
CA3 Registro en historia clínica 
CA4 Control de inventario 
CA5 Bloqueo de duplicación de citas 
CA6 Cancelación correcta

------------------------------------------------------------------------

DISEÑO DEL SISTEMA 
1. Paciente 
2. Doctor 
3. Cita Médica 
4. Medicamento
5. Historia Clínica 
6. EPS 
7. EPSService (coordinador)

------------------------------------------------------------------------

FLUJO GENERAL 
- Registrar paciente 
- Registrar doctor 
- Crear cita 
- Modificar/cancelar si PENDIENTE 
- Atender cita - Registrar consulta 
- Consultar historial 
- Listar información

------------------------------------------------------------------------

INSTALACIÓN MAVEN WINDOWS

1.  Verificar Java: java -version

2.  Descargar Maven ZIP y extraer en: C:Files

3.  Variables de entorno: MAVEN_HOME = C:Files-maven-3.9.x Agregar al
    PATH: C:Files-maven-3.9.x

4.  Verificar: mvn -version

5.  Ejecutar proyecto: cd mvn javafx:run
