(function () {
    'use strict';

    var loading = document.getElementById('citasLoading');
    var contenido = document.getElementById('citasContenido');
    var vacio = document.getElementById('citasVacio');
    var error = document.getElementById('citasError');
    var tablaBody = document.getElementById('citasTablaBody');
    var btnActualizar = document.getElementById('btnActualizarCitas');
    var btnReintentar = document.getElementById('btnReintentarCitas');
    var csrfToken = document.body.getAttribute('data-csrf') || '';
    var csrfName = document.body.getAttribute('data-csrf-name') || '_csrf';

    var estados = {
        PENDIENTE: { etiqueta: 'Pendiente', clase: 'pending' },
        CONFIRMADA: { etiqueta: 'Confirmada', clase: 'confirmed' },
        CANCELADA: { etiqueta: 'Cancelada', clase: 'cancelled' },
        COMPLETADA: { etiqueta: 'Completada', clase: 'completed' }
    };

    function ocultarEstados() {
        loading.hidden = true;
        contenido.hidden = true;
        vacio.hidden = true;
        error.hidden = true;
    }

    function establecerCargando() {
        ocultarEstados();
        loading.hidden = false;
        contenido.setAttribute('aria-busy', 'true');
        btnActualizar.disabled = true;
        btnActualizar.querySelector('span').textContent = 'Actualizando...';
    }

    function finalizarCarga() {
        contenido.setAttribute('aria-busy', 'false');
        btnActualizar.disabled = false;
        btnActualizar.querySelector('span').textContent = 'Actualizar';
    }

    function formatearFecha(fechaIso) {
        var partes = fechaIso.split('-').map(Number);
        var fecha = new Date(partes[0], partes[1] - 1, partes[2]);
        return new Intl.DateTimeFormat('es-SV', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        }).format(fecha);
    }

    function formatearHora(hora) {
        return hora ? hora.substring(0, 5) : '';
    }

    function crearCelda(texto, clase) {
        var celda = document.createElement('td');
        celda.textContent = texto;
        if (clase) {
            celda.className = clase;
        }
        return celda;
    }

    function crearFila(cita) {
        var fila = document.createElement('tr');
        var estado = estados[cita.estado] || {
            etiqueta: cita.estado || 'Sin estado',
            clase: 'neutral'
        };
        var celdaEstado = document.createElement('td');
        var insignia = document.createElement('span');

        fila.appendChild(crearCelda(formatearFecha(cita.fecha)));
        fila.appendChild(crearCelda(formatearHora(cita.hora)));
        fila.appendChild(crearCelda(cita.servicioNombre, 'cell-strong'));

        insignia.className = 'badge-soft ' + estado.clase;
        insignia.textContent = estado.etiqueta;
        celdaEstado.appendChild(insignia);
        fila.appendChild(celdaEstado);

        var celdaAcciones = document.createElement('td');
        var acciones = document.createElement('div');
        acciones.className = 'cita-actions';

        if (cita.estado === 'PENDIENTE' || cita.estado === 'CONFIRMADA') {
            var editar = document.createElement('a');
            editar.className = 'cita-action edit';
            editar.href = '/mis-citas/' + encodeURIComponent(cita.id) + '/editar';
            editar.setAttribute('aria-label', 'Editar cita de ' + cita.servicioNombre);
            editar.innerHTML = '<i class="bi bi-pencil-square" aria-hidden="true"></i> Editar';
            acciones.appendChild(editar);
        }

        var formularioEliminar = document.createElement('form');
        formularioEliminar.method = 'post';
        formularioEliminar.action = '/mis-citas/' + encodeURIComponent(cita.id) + '/eliminar';
        formularioEliminar.addEventListener('submit', function (evento) {
            if (!window.confirm('¿Deseas eliminar esta cita? Esta acción no se puede deshacer.')) {
                evento.preventDefault();
            }
        });

        if (csrfToken) {
            var csrf = document.createElement('input');
            csrf.type = 'hidden';
            csrf.name = csrfName;
            csrf.value = csrfToken;
            formularioEliminar.appendChild(csrf);
        }

        var eliminar = document.createElement('button');
        eliminar.type = 'submit';
        eliminar.className = 'cita-action delete';
        eliminar.setAttribute('aria-label', 'Eliminar cita de ' + cita.servicioNombre);
        eliminar.innerHTML = '<i class="bi bi-trash3" aria-hidden="true"></i> Eliminar';
        formularioEliminar.appendChild(eliminar);
        acciones.appendChild(formularioEliminar);
        celdaAcciones.appendChild(acciones);
        fila.appendChild(celdaAcciones);

        return fila;
    }

    function mostrarCitas(citas) {
        ocultarEstados();
        tablaBody.replaceChildren();

        if (!citas.length) {
            vacio.hidden = false;
            return;
        }

        citas.forEach(function (cita) {
            tablaBody.appendChild(crearFila(cita));
        });
        contenido.hidden = false;
    }

    function cargarCitas() {
        establecerCargando();

        fetch('/api/citas/usuario', {
            method: 'GET',
            headers: { 'Accept': 'application/json' },
            credentials: 'same-origin',
            cache: 'no-store'
        })
            .then(function (respuesta) {
                if (respuesta.status === 401) {
                    window.location.assign('/login');
                    return null;
                }
                if (!respuesta.ok) {
                    throw new Error('Error HTTP ' + respuesta.status);
                }
                return respuesta.json();
            })
            .then(function (citas) {
                if (citas !== null) {
                    mostrarCitas(Array.isArray(citas) ? citas : []);
                }
            })
            .catch(function () {
                ocultarEstados();
                error.hidden = false;
            })
            .finally(finalizarCarga);
    }

    btnActualizar.addEventListener('click', cargarCitas);
    btnReintentar.addEventListener('click', cargarCitas);
    window.addEventListener('pageshow', function (evento) {
        if (evento.persisted) {
            cargarCitas();
        }
    });

    cargarCitas();
})();
