# Moto Store Backend (Local con Web y Verificación de Correo)

Proyecto de ejemplo de **venta de motos** usando **Spring Boot 3**, **Thymeleaf** y **PostgreSQL**.

Incluye:

- Páginas web (Thymeleaf): inicio, login, registro, catálogo de motos.
- Registro de usuarios con verificación de correo por código PIN.
- Roles: `SUPER_ADMIN`, `CUSTOMER`.
- Gestión de motos con stock.
- Carrito y ventas (vía API) con generación de factura PDF (OpenPDF).

## Requisitos

- Java 17
- Maven
- PostgreSQL
- Cuenta SMTP (Mailtrap, Gmail, etc.) para enviar correos

## Configuración

1. Crear base de datos en PostgreSQL:

```sql
CREATE DATABASE moto_store;
```

2. Editar `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/moto_store
    username: TU_USUARIO
    password: TU_PASSWORD
```

3. Configurar credenciales de correo en la sección `spring.mail` (puedes usar Mailtrap para pruebas).

## Usuarios

Al iniciar la aplicación, se crea automáticamente un usuario administrador:

- Email: `admin@motos.com`
- Contraseña: `admin123`
- Rol: `SUPER_ADMIN`

## Ejecutar

En la raíz del proyecto:

```bash
mvn spring-boot:run
```

Luego abrir en el navegador:

- `http://localhost:8080/` → página de inicio
- `http://localhost:8080/register` → registro de usuario (envía código al correo)
- `http://localhost:8080/login` → iniciar sesión (usa el correo como usuario)
- `http://localhost:8080/motos` → catálogo de motos

API REST (por si la necesitas):

- `GET /api/motos` → lista de motos
- `POST /api/motos` (ADMIN) → crear moto
- `PUT /api/motos/{id}` (ADMIN) → actualizar moto
- `DELETE /api/motos/{id}` (ADMIN) → eliminar moto

Carrito y ventas:

- `GET /api/cart/{customerId}`
- `POST /api/cart/{customerId}/add?motoId=&quantity=`
- `POST /api/sales/checkout/{customerId}?paymentMethod=&deliveryType=`
- `GET /api/sales/{id}/invoice` → descarga factura PDF

## Próximo paso

Cuando todo funcione localmente, este mismo proyecto puede desplegarse en servicios como Render/Railway para que sea accesible desde cualquier lugar.
