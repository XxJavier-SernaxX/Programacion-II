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

### esta es una actualizacion de readme 
    en la rama feature-docu