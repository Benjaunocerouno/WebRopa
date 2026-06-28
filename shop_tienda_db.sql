-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost:3306
-- Tiempo de generación: 28-06-2026 a las 04:00:05
-- Versión del servidor: 10.11.18-MariaDB
-- Versión de PHP: 8.4.22

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `shop_tienda_db`
--
CREATE DATABASE IF NOT EXISTS `shop_tienda_db` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `shop_tienda_db`;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `boletas`
--

CREATE TABLE `boletas` (
  `id` bigint(20) NOT NULL,
  `numero_boleta` varchar(255) NOT NULL,
  `fecha_emision` datetime(6) NOT NULL DEFAULT current_timestamp(6),
  `nombre_cliente` varchar(255) NOT NULL,
  `dni_cliente` varchar(255) DEFAULT NULL,
  `subtotal` double NOT NULL,
  `igv` double NOT NULL,
  `total` double NOT NULL,
  `pedido_id` bigint(20) NOT NULL,
  `estado` enum('ACTIVA','ANULADA') NOT NULL DEFAULT 'ACTIVA'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `boletas`
--

INSERT INTO `boletas` (`id`, `numero_boleta`, `fecha_emision`, `nombre_cliente`, `dni_cliente`, `subtotal`, `igv`, `total`, `pedido_id`, `estado`) VALUES
(7, 'BOL-FD1F4C93', '2026-06-27 02:56:22.000000', 'cliente1', NULL, 67.71, 12.19, 79.9, 20, 'ACTIVA');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `carritos`
--

CREATE TABLE `carritos` (
  `id` bigint(20) NOT NULL,
  `usuario_id` bigint(20) NOT NULL,
  `estado` enum('ACTIVO','INACTIVO') DEFAULT 'ACTIVO'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `carritos`
--

INSERT INTO `carritos` (`id`, `usuario_id`, `estado`) VALUES
(29, 31, 'ACTIVO'),
(30, 32, 'ACTIVO'),
(31, 33, 'ACTIVO'),
(32, 34, 'ACTIVO'),
(33, 35, 'ACTIVO'),
(34, 36, 'ACTIVO'),
(35, 37, 'ACTIVO'),
(36, 38, 'ACTIVO'),
(37, 1, 'ACTIVO'),
(38, 2, 'ACTIVO'),
(39, 3, 'ACTIVO'),
(40, 4, 'ACTIVO'),
(41, 5, 'ACTIVO'),
(42, 6, 'ACTIVO'),
(43, 7, 'ACTIVO'),
(44, 8, 'ACTIVO');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `carrito_items`
--

CREATE TABLE `carrito_items` (
  `id` bigint(20) NOT NULL,
  `cantidad` int(11) DEFAULT NULL,
  `carrito_id` bigint(20) NOT NULL,
  `variante_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categorias`
--

CREATE TABLE `categorias` (
  `id` bigint(20) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `estado` enum('ACTIVO','INACTIVO') DEFAULT 'ACTIVO',
  `empresa_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `categorias`
--

INSERT INTO `categorias` (`id`, `descripcion`, `nombre`, `estado`, `empresa_id`) VALUES
(1, 'Ropa de verano', 'Polos', 'ACTIVO', 2),
(5, 'Ropa para abrigarse', 'Ropa Invierno', 'ACTIVO', 2),
(14, NULL, 'Chompas', 'ACTIVO', 1),
(15, NULL, 'Poleras', 'ACTIVO', 1),
(16, NULL, 'Tops', 'ACTIVO', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cupones`
--

CREATE TABLE `cupones` (
  `id` bigint(20) NOT NULL,
  `codigo` varchar(255) NOT NULL,
  `tipo` enum('PORCENTAJE','MONTO_FIJO') NOT NULL,
  `valor` double DEFAULT NULL,
  `minimo_compra` double DEFAULT NULL,
  `usos_maximos` int(11) DEFAULT NULL,
  `usos_actuales` int(11) DEFAULT 0,
  `fecha_inicio` date DEFAULT NULL,
  `fecha_fin` date DEFAULT NULL,
  `categoria_id` bigint(20) DEFAULT NULL,
  `producto_id` bigint(20) DEFAULT NULL,
  `estado` enum('ACTIVO','INACTIVO') DEFAULT 'ACTIVO',
  `empresa_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `cupones`
--

INSERT INTO `cupones` (`id`, `codigo`, `tipo`, `valor`, `minimo_compra`, `usos_maximos`, `usos_actuales`, `fecha_inicio`, `fecha_fin`, `categoria_id`, `producto_id`, `estado`, `empresa_id`) VALUES
(4, 'FULLSTACK', 'PORCENTAJE', 15, 150, 100, 0, NULL, NULL, NULL, NULL, 'ACTIVO', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cupon_usos`
--

CREATE TABLE `cupon_usos` (
  `id` bigint(20) NOT NULL,
  `cupon_id` bigint(20) NOT NULL,
  `usuario_id` bigint(20) NOT NULL,
  `pedido_id` bigint(20) NOT NULL,
  `fecha` datetime(6) DEFAULT current_timestamp(6),
  `estado` enum('APLICADO','REVERTIDO') DEFAULT 'APLICADO',
  `observacion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `devoluciones`
--

CREATE TABLE `devoluciones` (
  `id` bigint(20) NOT NULL,
  `fecha` datetime(6) DEFAULT current_timestamp(6),
  `motivo` enum('DEFECTUOSO','TALLA_INCORRECTA','COLOR_INCORRECTO','OTRO') NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `cantidad_devuelta` int(11) NOT NULL,
  `monto_reembolso` double NOT NULL,
  `estado` enum('SOLICITADA','APROBADA','RECHAZADA','REEMBOLSADA','CANCELADA') DEFAULT 'SOLICITADA',
  `pedido_id` bigint(20) NOT NULL,
  `pedido_item_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `empresas`
--

CREATE TABLE `empresas` (
  `id` bigint(20) NOT NULL,
  `ruc` varchar(11) NOT NULL,
  `razon_social` varchar(255) NOT NULL,
  `nombre_comercial` varchar(255) DEFAULT NULL,
  `estado` enum('ACTIVO','INACTIVO') DEFAULT 'ACTIVO'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `empresas`
--

INSERT INTO `empresas` (`id`, `ruc`, `razon_social`, `nombre_comercial`, `estado`) VALUES
(1, '12345678912', 'Ternos Gaston', 'Ternos Gaston', 'ACTIVO'),
(2, '12345678911', 'Platanitos Negros', 'Platanitos Negros', 'ACTIVO'),
(3, '12345678914', 'Andreas SAC', 'Andreas SAC', 'ACTIVO'),
(4, '12345678915', 'IMeza SAC', 'IMeza SAC', 'ACTIVO'),
(5, '12345678916', 'XLucas SEX', 'XLucas SEX', 'ACTIVO');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `inventario_movimientos`
--

CREATE TABLE `inventario_movimientos` (
  `id` bigint(20) NOT NULL,
  `tipo_movimiento` enum('INGRESO_COMPRA','SALIDA_VENTA','INGRESO_DEVOLUCION','AJUSTE_MANUAL') NOT NULL,
  `cantidad` int(11) NOT NULL,
  `observacion` varchar(255) DEFAULT NULL,
  `fecha` datetime(6) NOT NULL DEFAULT current_timestamp(6),
  `estado` enum('ACTIVO','ANULADO') DEFAULT 'ACTIVO',
  `variante_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `logs_inventario`
--

CREATE TABLE `logs_inventario` (
  `id` bigint(20) NOT NULL,
  `fecha` datetime(6) DEFAULT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  `valor_ajuste` int(11) NOT NULL,
  `valor_anterior` int(11) NOT NULL,
  `admin_id` bigint(20) NOT NULL,
  `variante_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pagos`
--

CREATE TABLE `pagos` (
  `id` bigint(20) NOT NULL,
  `fecha` datetime(6) NOT NULL DEFAULT current_timestamp(6),
  `monto` double NOT NULL,
  `metodo` enum('TARJETA','YAPE','PLIN','TRANSFERENCIA') NOT NULL,
  `estado` enum('PENDIENTE','APROBADO','RECHAZADO','REEMBOLSADO') DEFAULT 'PENDIENTE',
  `referencia_externa` varchar(255) DEFAULT NULL,
  `proveedor` varchar(255) DEFAULT NULL,
  `pedido_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `pagos`
--

INSERT INTO `pagos` (`id`, `fecha`, `monto`, `metodo`, `estado`, `referencia_externa`, `proveedor`, `pedido_id`) VALUES
(12, '2026-06-26 19:52:06.000000', 159.8, 'YAPE', 'PENDIENTE', '123498', NULL, 19),
(13, '2026-06-27 02:55:47.000000', 79.9, 'YAPE', 'APROBADO', '123415', NULL, 20);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pedidos`
--

CREATE TABLE `pedidos` (
  `id` bigint(20) NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL DEFAULT current_timestamp(6),
  `estado` enum('PENDIENTE','CONFIRMADO','EN_PREPARACION','LISTO_PARA_RECOGER','RECOGIDO','CANCELADO') DEFAULT 'PENDIENTE',
  `subtotal` double NOT NULL,
  `descuento` double NOT NULL,
  `total` double NOT NULL,
  `pago_confirmado` tinyint(1) DEFAULT 0,
  `notas` varchar(255) DEFAULT NULL,
  `usuario_id` bigint(20) NOT NULL,
  `cupon_id` bigint(20) DEFAULT NULL,
  `empresa_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `pedidos`
--

INSERT INTO `pedidos` (`id`, `fecha_creacion`, `estado`, `subtotal`, `descuento`, `total`, `pago_confirmado`, `notas`, `usuario_id`, `cupon_id`, `empresa_id`) VALUES
(19, '2026-06-26 19:52:00.000000', 'CANCELADO', 159.8, 0, 159.8, 0, 'Recojo en tienda', 2, NULL, 1),
(20, '2026-06-27 02:55:41.000000', 'LISTO_PARA_RECOGER', 79.9, 0, 79.9, 1, 'Recojo en tienda', 2, NULL, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pedido_items`
--

CREATE TABLE `pedido_items` (
  `id` bigint(20) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `precio_unitario` double DEFAULT NULL,
  `pedido_id` bigint(20) NOT NULL,
  `variante_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `pedido_items`
--

INSERT INTO `pedido_items` (`id`, `cantidad`, `precio_unitario`, `pedido_id`, `variante_id`) VALUES
(21, 2, 79.9, 19, 14),
(22, 1, 79.9, 20, 14);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `permisos`
--

CREATE TABLE `permisos` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `permisos`
--

INSERT INTO `permisos` (`id`, `nombre`, `descripcion`) VALUES
(1, 'PRODUCTOS_CREAR', 'Permite registrar nuevos productos en el catálogo de la empresa'),
(2, 'PRODUCTOS_EDITAR', 'Permite modificar datos e imágenes de productos existentes'),
(3, 'PRODUCTOS_ELIMINAR', 'Permite deshabilitar lógicamente productos del catálogo'),
(4, 'INVENTARIO_AJUSTAR', 'Permite realizar ingresos de mercadería y ajustes manuales de stock'),
(5, 'PEDIDOS_GESTIONAR', 'Permite cambiar estados de pedidos y procesar los recojos en tienda'),
(6, 'REPORTES_VER', 'Permite visualizar y exportar reportes económicos y de stock crítico'),
(7, 'PERSONAL_GESTIONAR', 'Permite registrar, editar y dar de baja personal de la empresa'),
(8, 'BOLETAS_ANULAR', 'Permite la gestión y anulación de comprobantes electrónicos de venta');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `productos`
--

CREATE TABLE `productos` (
  `id` bigint(20) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `estado` enum('ACTIVO','INACTIVO') DEFAULT 'ACTIVO',
  `imagen_url` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `precio` double DEFAULT NULL,
  `categoria_id` bigint(20) DEFAULT NULL,
  `empresa_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `productos`
--

INSERT INTO `productos` (`id`, `descripcion`, `estado`, `imagen_url`, `nombre`, `precio`, `categoria_id`, `empresa_id`) VALUES
(11, 'Modelo: PO.W.FULLZ.ST País de origen: China Condicion del producto: Nuevo Marca: MOUNTAIN GEAR Composición: 100%Poliéster Estilo de vestuario: Deportivo Género: Mujer Material de vestuario: Poliéster Tipo: Casaca deportiva', 'ACTIVO', 'https://media.falabella.com/falabellaPE/883665458_1/w=1200,h=1200,fit=pad', 'Casaca Polar Deportiva Outdoor Mujer Mountain Gear', 79.9, 14, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `recojo_tienda`
--

CREATE TABLE `recojo_tienda` (
  `id` bigint(20) NOT NULL,
  `codigo_recojo` varchar(255) NOT NULL,
  `estado` enum('PENDIENTE','LISTO_PARA_RECOGER','RECOGIDO','EXPIRADO') DEFAULT 'PENDIENTE',
  `fecha_disponible` datetime(6) DEFAULT NULL,
  `fecha_recogido` datetime(6) DEFAULT NULL,
  `pedido_id` bigint(20) NOT NULL,
  `atendido_por` bigint(20) DEFAULT NULL,
  `empresa_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `recojo_tienda`
--

INSERT INTO `recojo_tienda` (`id`, `codigo_recojo`, `estado`, `fecha_disponible`, `fecha_recogido`, `pedido_id`, `atendido_por`, `empresa_id`) VALUES
(4, 'REC-C936AE', 'PENDIENTE', NULL, NULL, 20, NULL, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `registros`
--

CREATE TABLE `registros` (
  `idregistro` int(11) NOT NULL,
  `nombres` varchar(255) NOT NULL,
  `apellidos` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `cliente_id` varchar(255) NOT NULL,
  `llave_secreta` varchar(255) NOT NULL,
  `access_token` varchar(255) DEFAULT NULL,
  `estado` int(11) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `registros`
--

INSERT INTO `registros` (`idregistro`, `nombres`, `apellidos`, `email`, `cliente_id`, `llave_secreta`, `access_token`, `estado`) VALUES
(4, 'BenjaMIN', 'RORO', 'benjarorororo@gmail.com', 'ec4a55ce5624d78c9ce11e9f6444c3e22a46b2b3307d9b654f73d530f3be4890', '$2a$10$.awDU7yfLDnRCOj/xKeFvelYefcR2CvhsBurKz90kFE1C1RXYgEsm', 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlYzRhNTVjZTU2MjRkNzhjOWNlMTFlOWY2NDQ0YzNlMjJhNDZiMmIzMzA3ZDliNjU0ZjczZDUzMGYzYmU0ODkwIiwicGVybWlzb3MiOltdLCJyb2wiOiJBUEkiLCJpYXQiOjE3ODI0OTIxODMsImV4cCI6NDkzNjA5MjE4M30.G4PerWY4M51Jxpvl14MtEurPj_pOvHQjElVQ4nDm1MM', 1),
(5, 'documento', 'documentoide', 'documento@gmail.com', '7c5fcfb096fb8a8629895c9268c09b0c336b3f463e192485970a6caa8842f717', '$2a$10$zRoHAywRdqKTMiH9J8sYkuhLybkqPGk8p5o24erGWgoMmBq8WkhX2', 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI3YzVmY2ZiMDk2ZmI4YTg2Mjk4OTVjOTI2OGMwOWIwYzMzNmIzZjQ2M2UxOTI0ODU5NzBhNmNhYTg4NDJmNzE3IiwicGVybWlzb3MiOltdLCJyb2wiOiJBUEkiLCJpYXQiOjE3ODI1MjczMzAsImV4cCI6NDkzNjEyNzMzMH0.7CYXxLHEgqFk6tnaJZxSEJT7BHGs3gV0tG74g-4P6Ds', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `resenas`
--

CREATE TABLE `resenas` (
  `id` bigint(20) NOT NULL,
  `calificacion` int(1) NOT NULL CHECK (`calificacion` between 1 and 5),
  `comentario` text DEFAULT NULL,
  `fecha` datetime(6) DEFAULT current_timestamp(6),
  `aprobada` tinyint(1) DEFAULT 0,
  `usuario_id` bigint(20) NOT NULL,
  `producto_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `roles`
--

CREATE TABLE `roles` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `roles`
--

INSERT INTO `roles` (`id`, `nombre`, `descripcion`) VALUES
(1, 'SUPERADMIN', 'Control total del sistema y configuraciones'),
(2, 'ADMIN', 'Administrador de la tienda y reportes'),
(3, 'EMPLEADO', 'Gestión de inventario y recojo en tienda'),
(4, 'CLIENTE', 'Comprador regular de la plataforma');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `roles_permisos`
--

CREATE TABLE `roles_permisos` (
  `rol_id` bigint(20) NOT NULL,
  `permiso_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `roles_permisos`
--

INSERT INTO `roles_permisos` (`rol_id`, `permiso_id`) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(1, 8),
(2, 1),
(2, 2),
(2, 3),
(2, 4),
(2, 5),
(2, 6),
(2, 7),
(2, 8),
(3, 4),
(3, 5);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` bigint(20) NOT NULL,
  `correo` varchar(255) NOT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `rol_id` bigint(20) NOT NULL,
  `empresa_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `correo`, `estado`, `nombre`, `password`, `telefono`, `rol_id`, `empresa_id`) VALUES
(1, 'benjapapu@gmail.solopapus', 'ACTIVO', 'Benja', '$2a$10$XvMlF9YHxDJ/nzDd0.qLy.wcA3T.DwgT.Me5Cp2VNhxA8KEbx3f0S', '987678987', 2, 1),
(2, 'cliente1@gmail.com', 'ACTIVO', 'cliente1', '$2a$10$zhnZDEs/pvL2oapIXyLkz.n7MyI9b3TllK/jv4TrIfIv71KGCDJ/q', '912912945', 4, NULL),
(4, 'kristell@gmail.com', 'ACTIVO', 'Kristell', '$2a$10$LncjupLTBsOOSGWcCdhZl.hdpfQhW7Y93skUo.6R/Lm1ahdkejnbi', '996760188', 2, 3),
(5, 'ibericomeza@gmail.com', 'ACTIVO', 'Jose Alonso', '$2a$10$3aAjsfUHmkrS/uHyR9oMfuiz98TqY.8nJkNlabL0lSlr8UnfodPUO', '987654321', 2, 4),
(6, 'prueba@gmail.com', 'ACTIVO', 'prueba', '$2a$10$YBBSnj7uqpRKgxTYTV/sHe8B7LcztmHSi3yR31boOzrLANl6u2X8y', '942218870', 4, NULL),
(7, 'superadmin@gmail.com', 'ACTIVO', 'superadmin', '$2a$10$/VngHBbqucKf3zuv/Ek8MOtpsE9syQX0nihhy2HauHQbH.pvbJnMW', '942218870', 1, NULL),
(8, 'amy@gmail.com', 'ACTIVO', 'Amy', '$2a$10$JpAkNv0/fuoFoM4jHf2BO..5p/ftfeDh/XgomZOtiaxoH2cpUn9Ey', '942218870', 2, 2);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `variantes`
--

CREATE TABLE `variantes` (
  `id` bigint(20) NOT NULL,
  `color` varchar(255) NOT NULL,
  `sku` varchar(255) NOT NULL,
  `stock` int(11) NOT NULL,
  `stock_critico` int(11) NOT NULL,
  `talla` varchar(255) NOT NULL,
  `producto_id` bigint(20) NOT NULL,
  `estado` enum('ACTIVO','INACTIVO') DEFAULT 'ACTIVO',
  `imagen` varchar(255) DEFAULT NULL,
  `hex` varchar(255) DEFAULT NULL,
  `empresa_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `variantes`
--

INSERT INTO `variantes` (`id`, `color`, `sku`, `stock`, `stock_critico`, `talla`, `producto_id`, `estado`, `imagen`, `hex`, `empresa_id`) VALUES
(13, 'azul', 'VAR-318-YD', 10, 2, 'M', 10, 'INACTIVO', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRcVKYTR7v6tKdK4or9Y_95-1KhUpMvD3LysyT8ZR-JlA&s=10', '#367ad3', 1),
(14, 'Verde', 'VAR-996-BJ', 52, 2, 'M', 11, 'ACTIVO', 'https://media.falabella.com/falabellaPE/883665455_1/w=1200,h=1200,fit=pad', '#00ff00', 1),
(15, 'Rosado', 'VAR-496-WY', 49, 2, 'L', 11, 'INACTIVO', 'https://media.falabella.com/falabellaPE/883275792_1/w=1200,h=1200,fit=pad', '#ffc0cb', 1);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `boletas`
--
ALTER TABLE `boletas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero_boleta` (`numero_boleta`),
  ADD UNIQUE KEY `pedido_id` (`pedido_id`);

--
-- Indices de la tabla `carritos`
--
ALTER TABLE `carritos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKdqb2sn4sl6ioxpxtm72doib9p` (`usuario_id`);

--
-- Indices de la tabla `carrito_items`
--
ALTER TABLE `carrito_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKrtdnie5q1ntdbm7edy7n7fmvd` (`carrito_id`),
  ADD KEY `FKotwgc7uxpwiotafonyvo73487` (`variante_id`);

--
-- Indices de la tabla `categorias`
--
ALTER TABLE `categorias`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKqcog8b7hps1hioi9onqwjdt6y` (`nombre`),
  ADD KEY `fk_categoria_empresa` (`empresa_id`);

--
-- Indices de la tabla `cupones`
--
ALTER TABLE `cupones`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `codigo` (`codigo`),
  ADD KEY `FKd8mv5o9waoq6xx3eil8qxvucs` (`categoria_id`),
  ADD KEY `FKal3ucl18sffpv48xkdkkbh4vh` (`producto_id`),
  ADD KEY `fk_cupon_empresa` (`empresa_id`);

--
-- Indices de la tabla `cupon_usos`
--
ALTER TABLE `cupon_usos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uso_unico` (`cupon_id`,`usuario_id`),
  ADD KEY `usuario_id` (`usuario_id`),
  ADD KEY `pedido_id` (`pedido_id`);

--
-- Indices de la tabla `devoluciones`
--
ALTER TABLE `devoluciones`
  ADD PRIMARY KEY (`id`),
  ADD KEY `pedido_id` (`pedido_id`),
  ADD KEY `pedido_item_id` (`pedido_item_id`);

--
-- Indices de la tabla `empresas`
--
ALTER TABLE `empresas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_empresa_ruc` (`ruc`);

--
-- Indices de la tabla `inventario_movimientos`
--
ALTER TABLE `inventario_movimientos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_movimiento_variante` (`variante_id`);

--
-- Indices de la tabla `logs_inventario`
--
ALTER TABLE `logs_inventario`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKb6lvjis5hwn0rw7qj1xtnaqqt` (`admin_id`),
  ADD KEY `FKb3hgsrpovncaams95ud0f5k5h` (`variante_id`);

--
-- Indices de la tabla `pagos`
--
ALTER TABLE `pagos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `pedido_id` (`pedido_id`);

--
-- Indices de la tabla `pedidos`
--
ALTER TABLE `pedidos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `usuario_id` (`usuario_id`),
  ADD KEY `cupon_id` (`cupon_id`),
  ADD KEY `fk_pedido_empresa` (`empresa_id`);

--
-- Indices de la tabla `pedido_items`
--
ALTER TABLE `pedido_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `pedido_id` (`pedido_id`),
  ADD KEY `variante_id` (`variante_id`);

--
-- Indices de la tabla `permisos`
--
ALTER TABLE `permisos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_permiso_nombre` (`nombre`);

--
-- Indices de la tabla `productos`
--
ALTER TABLE `productos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre` (`nombre`),
  ADD KEY `FK2fwq10nwymfv7fumctxt9vpgb` (`categoria_id`),
  ADD KEY `fk_producto_empresa` (`empresa_id`);

--
-- Indices de la tabla `recojo_tienda`
--
ALTER TABLE `recojo_tienda`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `codigo_recojo` (`codigo_recojo`),
  ADD KEY `pedido_id` (`pedido_id`),
  ADD KEY `fk_recojo_atendido` (`atendido_por`),
  ADD KEY `FKtnv7k998adolei15o79bcu0jg` (`empresa_id`);

--
-- Indices de la tabla `registros`
--
ALTER TABLE `registros`
  ADD PRIMARY KEY (`idregistro`);

--
-- Indices de la tabla `resenas`
--
ALTER TABLE `resenas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unica_resena` (`usuario_id`,`producto_id`),
  ADD KEY `fk_resena_producto` (`producto_id`);

--
-- Indices de la tabla `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_rol_nombre` (`nombre`);

--
-- Indices de la tabla `roles_permisos`
--
ALTER TABLE `roles_permisos`
  ADD PRIMARY KEY (`rol_id`,`permiso_id`),
  ADD KEY `fk_rp_permiso` (`permiso_id`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKcdmw5hxlfj78uf4997i3qyyw5` (`correo`),
  ADD KEY `fk_usuario_rol` (`rol_id`),
  ADD KEY `fk_usuario_empresa` (`empresa_id`);

--
-- Indices de la tabla `variantes`
--
ALTER TABLE `variantes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKtpv26x89gfjtlh97kj4djyn0b` (`sku`),
  ADD KEY `FKqy7ffq0bdlclwb5vgpwn20m02` (`producto_id`),
  ADD KEY `fk_variantes_empresa` (`empresa_id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `boletas`
--
ALTER TABLE `boletas`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `carritos`
--
ALTER TABLE `carritos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=45;

--
-- AUTO_INCREMENT de la tabla `carrito_items`
--
ALTER TABLE `carrito_items`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- AUTO_INCREMENT de la tabla `categorias`
--
ALTER TABLE `categorias`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT de la tabla `cupones`
--
ALTER TABLE `cupones`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `cupon_usos`
--
ALTER TABLE `cupon_usos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `devoluciones`
--
ALTER TABLE `devoluciones`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `empresas`
--
ALTER TABLE `empresas`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `inventario_movimientos`
--
ALTER TABLE `inventario_movimientos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `logs_inventario`
--
ALTER TABLE `logs_inventario`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `pagos`
--
ALTER TABLE `pagos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT de la tabla `pedidos`
--
ALTER TABLE `pedidos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT de la tabla `pedido_items`
--
ALTER TABLE `pedido_items`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT de la tabla `permisos`
--
ALTER TABLE `permisos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT de la tabla `productos`
--
ALTER TABLE `productos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT de la tabla `recojo_tienda`
--
ALTER TABLE `recojo_tienda`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `registros`
--
ALTER TABLE `registros`
  MODIFY `idregistro` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `resenas`
--
ALTER TABLE `resenas`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `roles`
--
ALTER TABLE `roles`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT de la tabla `variantes`
--
ALTER TABLE `variantes`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `boletas`
--
ALTER TABLE `boletas`
  ADD CONSTRAINT `fk_boleta_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedidos` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
