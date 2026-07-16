# Releases de El pesetero

Aquí están los APK listos para instalar directamente en cualquier teléfono
Android (8.0 / API 26 o superior), sin pasar por Google Play.

| Versión | Archivo | Tamaño | Estado |
|---------|---------|--------|--------|
| 1.2.0 | [el-pesetero-1.2.0.apk](el-pesetero-1.2.0.apk) | ~3 MB | **Actual** |
| 1.0.0 | [peseta-1.0.0.apk](peseta-1.0.0.apk) | ~3 MB | Anterior |

Descarga siempre la versión marcada como **Actual**. Las anteriores se conservan solo
como histórico de versiones.

### Novedades de la 1.2.0

- Presupuestos mensuales por categoría, con aviso y barra de progreso tipo semáforo.
- Movimientos recurrentes que se apuntan solos al abrir la app.
- Foto de ticket adjunta a cada movimiento.
- Multi-divisa: cada cuenta en su moneda y saldo total combinado con tasas manuales.
- Objetivos de ahorro con aportaciones manuales.
- Etiquetas para agrupar gastos entre categorías.

## Cómo instalarlo en tu móvil

1. Copia el archivo `el-pesetero-1.2.0.apk` a tu teléfono (por cable USB, correo, Drive, etc.).
2. En el móvil, abre el archivo desde el explorador de archivos.
3. La primera vez, Android te pedirá permitir **instalar apps de orígenes desconocidos**
   para la app desde la que abres el APK (por ejemplo, tu explorador de archivos o el navegador).
   Actívalo y vuelve a pulsar el APK.
4. Pulsa **Instalar**. Listo.

No necesitas cuenta, conexión ni permisos especiales: la app funciona 100 % sin internet.

## Notas

- Estos APK están firmados con la clave de firma del proyecto. Manténla siempre igual
  para poder actualizar la app sin desinstalarla (una firma distinta obliga a reinstalar).
- Para generar un nuevo APK de release: `./gradlew assembleRelease`
  (el resultado queda en `app/build/outputs/apk/release/`).
