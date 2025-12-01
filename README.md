📂 Gestor de Archivos IPN / ESCOM

Una aplicación nativa de Android desarrollada en Kotlin y Jetpack Compose para la gestión eficiente de archivos, con una interfaz moderna y personalizable que refleja la identidad institucional del Instituto Politécnico Nacional (IPN) y la Escuela Superior de Cómputo (ESCOM).

✨ Características Principales

🚀 Navegación y Gestión

Explorador de Archivos: Navegación fluida por el almacenamiento interno y externo.

Breadcrumbs: Indicadores visuales de la ruta actual para una fácil navegación.

Operaciones CRUD: Visualizar y eliminar archivos directamente desde la app.

Abrir con...: Integración con el sistema Android para abrir archivos no soportados nativamente por la app.

🎨 Personalización y UI

Temas Institucionales:

🔴 Tema Guinda (IPN): Colores representativos del Politécnico.

🔵 Tema Azul (ESCOM): Colores representativos de la ESCOM.

Modo Oscuro/Claro: Adaptación automática o manual a las preferencias del sistema.

Diseño Responsivo: Interfaz construida con Material Design 3 totalmente adaptable.

👁️ Visualizadores Integrados

Imágenes: Visor con soporte para gestos (Zoom, Panner y Rotación).

Texto y Código: Editor de lectura para archivos .txt, .md, .java, .kt, etc.

JSON Formatter: Visualización automática de archivos .json con formato "pretty print".

💾 Persistencia de Datos

Favoritos: Marca carpetas o archivos importantes para acceso rápido (Almacenado en Room Database).

Historial: Registro automático de los últimos archivos abiertos.

🛠️ Stack Tecnológico

Este proyecto utiliza las tecnologías más recientes del ecosistema Android (2024-2025):

Lenguaje: Kotlin 2.0+

Interfaz de Usuario: Jetpack Compose (Material 3)

Base de Datos: Room Database (SQLite) con KSP.

Carga de Imágenes: Coil

Navegación: Navigation Compose

Manejo de JSON: Gson

Build System: Gradle KTS

⚙️ Configuración del Proyecto

Requisitos Previos

Android Studio Koala o superior.

JDK 17 o superior.

Dispositivo o Emulador con Android 8.0 (API 26) o superior.

Instalación

Clona este repositorio:

Abre el proyecto en Android Studio.

Sincroniza el proyecto con Gradle (Sync Project with Gradle Files).

Nota sobre Permisos (Android 11+)

Al ejecutar la aplicación por primera vez en dispositivos con Android 11 o superior, se te redirigirá a la configuración del sistema para otorgar el permiso "Acceso a todos los archivos". Este permiso es obligatorio (MANAGE_EXTERNAL_STORAGE) para que la aplicación funcione como un gestor de archivos real.

📱 Estructura del Proyecto

com.example.gestorarchivosipn
├── data/                  # Capa de Datos
│   └── DatabaseEntities.kt # Entidades Room y DAO
├── ui/theme/              # Capa de Diseño
│   ├── Theme.kt           # Definición de Temas (Guinda/Azul)
│   └── Color.kt           # Paleta de colores
├── FileViewModel.kt       # Lógica de Negocio (MVVM)
├── FileScreens.kt         # Componentes UI (Composable functions)
└── MainActivity.kt        # Entry Point y Configuración de Navegación
