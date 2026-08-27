(function () {
    var texto = document.getElementById('filtroTexto');
    var categoria = document.getElementById('filtroCategoria');
    var precioMin = document.getElementById('filtroPrecioMin');
    var precioMax = document.getElementById('filtroPrecioMax');
    var duracion = document.getElementById('filtroDuracion');
    var limpiar = document.getElementById('limpiarFiltros');
    var resultados = document.getElementById('serviceResults');
    var sinResultados = document.getElementById('serviceEmptyState');
    var cargando = document.getElementById('serviceLoadingState');
    var contador = document.getElementById('serviceResultCount');
    var debounceId;
    var controller;
    var opcionesCargadas = false;

    function crearElemento(tag, clase, contenido) {
        var elemento = document.createElement(tag);
        if (clase) elemento.className = clase;
        if (contenido !== undefined && contenido !== null) elemento.textContent = contenido;
        return elemento;
    }

    function cargarOpciones(servicios) {
        if (opcionesCargadas) return;
        var categorias = [];
        var duraciones = [];
        servicios.forEach(function (servicio) {
            if (servicio.categoriaNombre && categorias.indexOf(servicio.categoriaNombre) === -1) {
                categorias.push(servicio.categoriaNombre);
            }
            if (servicio.duracionMinutos && duraciones.indexOf(servicio.duracionMinutos) === -1) {
                duraciones.push(servicio.duracionMinutos);
            }
        });
        categorias.sort().forEach(function (nombre) {
            categoria.add(new Option(nombre, nombre));
        });
        duraciones.sort(function (a, b) { return a - b; }).forEach(function (minutos) {
            duracion.add(new Option(minutos + ' min', minutos));
        });
        opcionesCargadas = true;
    }

    function crearTarjeta(servicio) {
        var tarjeta = crearElemento('article', 'app-card service-card');
        tarjeta.appendChild(crearElemento('span', 'service-category', servicio.categoriaNombre || 'Sin categoría'));
        tarjeta.appendChild(crearElemento('h2', 'service-name', servicio.nombre));
        tarjeta.appendChild(crearElemento('p', 'service-desc', servicio.descripcion || 'Sin descripción disponible.'));

        var pie = crearElemento('div', 'service-footer');
        var precio = Number(servicio.precio || 0).toLocaleString('es-SV', {
            style: 'currency', currency: 'USD', minimumFractionDigits: 2
        });
        pie.appendChild(crearElemento('span', 'service-price', precio));
        var tiempo = crearElemento('span', 'service-duration');
        var icono = crearElemento('i', 'bi bi-clock me-1');
        icono.setAttribute('aria-hidden', 'true');
        tiempo.appendChild(icono);
        tiempo.appendChild(document.createTextNode((servicio.duracionMinutos || 0) + ' min'));
        pie.appendChild(tiempo);
        tarjeta.appendChild(pie);
        return tarjeta;
    }

    function parametros() {
        var valores = new URLSearchParams();
        if (texto.value.trim()) valores.set('query', texto.value.trim());
        if (categoria.value) valores.set('categoria', categoria.value);
        if (precioMin.value) valores.set('precioMin', precioMin.value);
        if (precioMax.value) valores.set('precioMax', precioMax.value);
        if (duracion.value) valores.set('duracion', duracion.value);
        return valores;
    }

    function mostrarCarga(mostrar) {
        cargando.classList.toggle('d-none', !mostrar);
        resultados.setAttribute('aria-busy', String(mostrar));
    }

    function renderizar(servicios) {
        resultados.replaceChildren();
        servicios.forEach(function (servicio) { resultados.appendChild(crearTarjeta(servicio)); });
        sinResultados.classList.toggle('d-none', servicios.length !== 0);
        contador.textContent = servicios.length === 1 ? '1 servicio encontrado' : servicios.length + ' servicios encontrados';
    }

    function buscar() {
        if (controller) controller.abort();
        controller = new AbortController();
        mostrarCarga(true);
        fetch('/api/servicios?' + parametros().toString(), {
            headers: { 'Accept': 'application/json' }, cache: 'no-store', signal: controller.signal
        })
            .then(function (respuesta) {
                if (!respuesta.ok) throw new Error('No se pudieron cargar los servicios.');
                return respuesta.json();
            })
            .then(function (servicios) {
                cargarOpciones(servicios);
                renderizar(servicios || []);
            })
            .catch(function (error) {
                if (error.name === 'AbortError') return;
                resultados.replaceChildren();
                sinResultados.classList.remove('d-none');
                sinResultados.querySelector('strong').textContent = 'No se pudieron cargar los servicios';
                sinResultados.querySelector('span').textContent = 'Intenta actualizar la página nuevamente.';
                contador.textContent = '';
            })
            .finally(function () { mostrarCarga(false); });
    }

    function buscarConDebounce() {
        window.clearTimeout(debounceId);
        debounceId = window.setTimeout(buscar, 300);
    }

    [texto, categoria, precioMin, precioMax, duracion].forEach(function (control) {
        control.addEventListener('input', buscarConDebounce);
        control.addEventListener('change', buscarConDebounce);
    });
    limpiar.addEventListener('click', function () {
        texto.value = '';
        categoria.value = '';
        precioMin.value = '';
        precioMax.value = '';
        duracion.value = '';
        buscar();
    });

    buscar();
})();
