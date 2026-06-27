// ============================
// CONFIGURACIÓN DEL FRONTEND
// ============================
// Cambia API_URL cuando subas tu backend a la nube.
const CONFIG = {
  // ← Cambia esta URL por la de tu backend en la nube
  API_URL: 'http://shop.spring.informaticapp.com:2100/api',

  // Pega aquí el token generado en tu otra herramienta. 
  // Si está vacío (''), se usará el inicio de sesión normal.
  HARDCODED_TOKEN: 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlYzRhNTVjZTU2MjRkNzhjOWNlMTFlOWY2NDQ0YzNlMjJhNDZiMmIzMzA3ZDliNjU0ZjczZDUzMGYzYmU0ODkwIiwicGVybWlzb3MiOltdLCJyb2wiOiJBUEkiLCJpYXQiOjE3ODI0OTIxODMsImV4cCI6NDkzNjA5MjE4M30.G4PerWY4M51Jxpvl14MtEurPj_pOvHQjElVQ4nDm1MM',

  // Claves de localStorage
  TOKEN_KEY: 'webropa_user',
  TENANT_SESSION_KEY: 'tenant_session',
};
