# Programacion-II
Proyecto de grupo de programación II

Nombre del proyecto:

El proyecto consiste en la creación de un backend para la gestión de una Entidad Promotora de Salud (EPS), que permitirá  organizar la información de los usuarios y los servicios médicos.
Dentro del sistema se podrán registrar pacientes con sus datos personales, agregar médicos con sus especialidades, así como programar y administrar citas médicas de manera ordenada. Además, se tendrá la posibilidad de llevar un historial clínico para cada paciente, en el cual se registren diagnósticos, tratamientos y observaciones realizadas por los médicos.
Este sistema busca optimizar la administración de la EPS, garantizando un mejor seguimiento de los pacientes y una atención más organizada.


## Propósito del proyecto

- Digitalizar y organizar la información de la EPS.
- Permitir el registro, modificación y consulta de usuarios, médicos y citas.
- Garantizar un flujo ordenado para la asignación de citas médicas.
- Ofrecer una base sólida para integrar otros servicios de salud (historial clínico, tratamientos, facturación) en el futuro.


## Principales Objetos con sus atributos y métodos

### Paciente
- Atributos: identificación, nombre, edad, dirección, teléfono, EPS.  
- Métodos: actualizar datos personales, consultar información, solicitar cita médica, ver historial clínico.


### Doctor
- Atributos: identificación, nombre, especialidad, horarios disponibles.  
- Métodos: atender paciente, registrar diagnóstico, recetar medicamentos, programar horarios de atención.


### Cita Médica
- Atributos: paciente, doctor, fecha y hora, motivo de consulta, estado (pendiente, atendida, cancelada).  
- Métodos: asignar cita, cancelar cita, consultar detalles de la cita, cambiar fecha y hora.


### Medicamento
- Atributos: nombre, dosis, cantidad disponible, fecha de vencimiento.  
- Métodos: dispensar medicamento, consultar disponibilidad en stock, actualizar inventario.


### Historia Clínica
- Atributos: paciente, lista de consultas, diagnósticos, tratamientos, medicamentos formulados.  
- Métodos: registrar nueva consulta, ver historial completo, agregar diagnóstico y tratamiento.


###  EPS (Entidad de Salud)
- Atributos: nombre, lista de pacientes, lista de doctores, lista de servicios.  
- Métodos: registrar paciente, contratar doctor, consultar pacientes de la EPS, consultar doctores de la EPS.



## 1 – Requerimientos Funcionales (RF)

RF1. Registrar Paciente con datos personales (identificación, nombre, edad, dirección, teléfono, EPS).
RF2. Registrar Doctor con especialidad y horarios disponibles.
RF3. Crear Cita Médica en estado PENDIENTE asignando paciente, doctor, fecha, hora y motivo.
RF4. Modificar Cita Médica (cambiar fecha, hora o motivo) solo si está en estado PENDIENTE.
RF5. Cancelar Cita Médica → estado pasa a CANCELADA.
RF6. Atender Cita → estado pasa a ATENDIDA y se genera un registro en la Historia Clínica.
RF7. Registrar Diagnóstico y Tratamiento como parte de la consulta atendida.
RF8. Gestionar Inventario de Medicamentos (consultar stock, actualizar cantidad disponible).
RF9. Registrar Medicamentos Formulados para una consulta en el historial clínico.
RF10. Consultar Historia Clínica Completa de cualquier paciente.
RF11. Listar Pacientes y Doctores registrados en la EPS.

2 – Reglas del Negocio
Citas Médicas

Una cita puede estar en estado:

PENDIENTE → editable

ATENDIDA → no editable

CANCELADA → no editable

Un doctor solo puede atender citas dentro de su horario disponible.

Un paciente no puede tener dos citas con el mismo doctor en la misma fecha y hora.

Historia Clínica

Se crea automáticamente cuando se ATIENDE una cita.

Debe incluir diagnóstico, tratamiento y medicamentos formulados.

No es modificable una vez almacenada (solo se agregan nuevas consultas).

Medicamentos

Todo medicamento debe tener: nombre, dosis, cantidad disponible y fecha de vencimiento.

No se pueden formular medicamentos sin stock.

La cantidad disponible disminuye al formularse.

Validaciones

Datos obligatorios para registrar pacientes y doctores.

Fecha y hora de cita deben ser futuras.

No se puede atender una cita si el doctor no está disponible en ese horario.

No se puede editar una cita ATENDIDA o CANCELADA.

Medicamentos con cantidad ≤ 0 no pueden ser formulados.

Restricciones

No se incluye módulo de facturación.

No se maneja autenticación (simplificado).

No hay integración con bases de datos externas (en versión inicial).

Consola o API básica.

3 – Casos de Aceptación (CA)
CA1. Crear Cita solo si doctor está disponible

Dado que un doctor tiene un horario disponible de 8:00 a 12:00,
Cuando se intenta crear una cita a las 14:00,
Entonces el sistema debe rechazarla por fuera del horario permitido.

CA2. Cita editable solo si está en estado PENDIENTE

Dado que una cita está en estado ATENDIDA,
Cuando el usuario intenta cambiar la fecha u hora,
Entonces el sistema debe impedir la edición.

CA3. Registro de diagnóstico al atender cita

Dado que se atiende una cita médica,
Cuando el doctor registra diagnóstico y tratamiento,
Entonces el sistema debe crear una nueva entrada en la historia clínica del paciente.

CA4. Control de stock de medicamentos

Dado que un medicamento tiene cantidad disponible de 0,
Cuando el doctor intenta formularlo,
Entonces el sistema debe rechazar la operación e indicar falta de inventario.

CA5. Evitar citas duplicadas

Dado que un paciente ya tiene una cita con un doctor a una hora específica,
Cuando intenta agendar otra cita en la misma fecha y hora,
Entonces el sistema debe evitar la duplicación.

CA6. Cancelación de Cita

Dado una cita en estado PENDIENTE,
Cuando el usuario la cancela,
Entonces el sistema debe cambiar su estado a CANCELADA, evitando posteriores modificaciones.

4 – Diseño del Sistema
1. Paciente

Atributos: identificación, nombre, edad, dirección, teléfono, EPS.
Métodos: actualizar_datos(), consultar(), solicitar_cita(), ver_historial().

2. Doctor

Atributos: identificación, nombre, especialidad, horarios_disponibles.
Métodos: atender_paciente(), registrar_diagnóstico(), formular_medicamento(), programar_horarios().

3. Cita Médica

Atributos: paciente, doctor, fecha, hora, motivo, estado.
Métodos: asignar(), cancelar(), consultar(), reprogramar().

4. Medicamento

Atributos: nombre, dosis, cantidad_disponible, fecha_vencimiento.
Métodos: dispensar(), consultar_stock(), actualizar_inventario().

5. Historia Clínica

Atributos: paciente, lista_consultas (diagnóstico, tratamiento, medicamentos).
Métodos: registrar_consulta(), ver_historial(), agregar_diagnóstico().

6. EPS

Atributos: nombre, pacientes, doctores, servicios.
Métodos: registrar_paciente(), contratar_doctor(), listar_pacientes(), listar_doctores().

7. EPSService (Coordinador de Casos de Uso)

Encargado de:

Crear pacientes

Registrar doctores

Crear y administrar citas

Procesar atención médica

Actualizar historia clínica

Gestionar medicamentos

Listar información general

5 – Flujo General (Consola o API)

Registrar nuevo paciente.

Registrar doctor con su especialidad y horario.

Crear una cita → estado PENDIENTE.

Modificar o cancelar cita mientras siga PENDIENTE.

Atender cita → registrar diagnóstico, tratamiento y medicamentos.

Registrar consulta en Historia Clínica.

Consultar historial completo cuando sea necesario.

Listar pacientes, doctores y citas.

6 – Estructura del Proyecto