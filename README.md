
```sql

INSERT INTO roles (nombre) VALUES ('admin');
INSERT INTO roles (nombre) VALUES ('docente');
-- clave 12345
INSERT INTO usuarios (login, clave, status) VALUES ('sysadmin', '$2a$12$r74HSGhuNB5zqfLG3fiao.OlRzKsPv/6R5EuhLNeFqjkKM7BZJ20m', 1);

CREATE TABLE usuario_rol (
    usuario_id INT NOT NULL,
    rol_id INT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (rol_id) REFERENCES roles(id)
);

INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuarios u, roles r
WHERE u.login = 'sysadmin' AND r.nombre = 'admin';

```

```bash

mvn spring-boot:run

```