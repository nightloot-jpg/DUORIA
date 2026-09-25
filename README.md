# DUORIA — Espacio Íntimo Compartido

DUORIA es una aplicación nativa para Android desarrollada con Kotlin y Jetpack Compose, pensada como un espacio exclusivo y privado para parejas a distancia.

## Características Principales

1. **Hogar (Espacio Compartido):**
   - Relojes digitales en vivo con actualización por segundo para ambas zonas horarias (Madrid / Tokio).
   - Datos meteorológicos, estado de actividad y porcentaje de batería.
   - Contador regresivo en tiempo real para el próximo reencuentro (Días, Horas, Minutos, Segundos).
   - Botón de Latido en directo con pulsación táctil háptica (Vibrator API), ondas animadas y efecto visual de pulso.
   - Pizarrón interactivo de notas y recordatorios compartidos con gestión completa (añadir, completar y eliminar).

2. **Conexión (Preguntas y Retos Diarios):**
   - **Diaria:** Pregunta del día con mecánica de doble revelación (desbloqueo al responder) e hilo de comentarios íntimos.
   - **Retos:** Retos colaborativos ("Cocinar lo mismo", "Foto espontánea", "Playlist cruzada", "Misma luna") con marcadores sincronizados para ambos.
   - **Secretas:** Creación de preguntas secretas individuales con respuestas privadas.

3. **Sala de Cine (Watch Party en Directo):**
   - Reproductor multimedia con ExoPlayer y selector de vídeos de muestra o enlaces directos.
   - Indicador de sincronización en tiempo real ("Sincronizado · 2 viendo").
   - Reacciones con emojis flotantes animados sobre la pantalla.
   - Canal de voz con Yuki y chat en directo con respuestas inmediatas.

4. **Nuestro Feed (Instagram para Dos):**
   - Historias superiores con reproductor a pantalla completa con barra de progreso temporizada.
   - Vista de Feed con tarjetas detalladas y vista de cuadrícula 3x3 de recuerdos.
   - Doble toque en foto para dar me gusta ("1/1 ❤️").
   - Reproductor de notas de voz con visualizador de ondas sonoras y temporizador regresivo.
   - Hilo interactivo de comentarios en cada publicación.

5. **Bóveda Íntima (Espacio Protegido):**
   - Bloqueo por teclado PIN numérico (demo: `1402`) y sensor biométrico de huella dactilar.
   - **Desire Match:** Baraja de cartas de deseos íntimos con deslizamiento interactivo (Sí / No), detección de doble coincidencia y animación de match.
   - **Recuerdos:** Notas de voz cifradas y galería protegida.

## Arquitectura y Tecnologías
- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (Material Design 3)
- **Patrón:** MVVM con StateFlow y ViewModel
- **Media:** AndroidX Media3 ExoPlayer
- **Imágenes:** Coil Compose y drawables optimizados
- **Hápticos:** Android Vibrator / VibrationEffect
