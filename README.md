# BINGO
## Trabajo de grado
### Una nueva experiencia de compra
- Johan Sebastián Cáceres Chararí 
- Laura Isabela Martínez Galindo
- **Asesor:** Nicolás Cardozo
# ¿Qué es Bingo?
Es un proyecto que pretende estudiar diferentes tipologías de usuarios con diversidad de gustos y métodos de compra, para detectar cuáles son esos aspectos que hacen que la experiencia de hacer compras sea memorable y cuáles son aquellos que generan inconvenientes, con el fin de cambiar la manera en la que hacemos compras por medio de la observación y el análisis del comportamiento de las personas al momento de hacer compras.  (Ceballos, 2023)
# Funcionalidades
## Búsqueda avanzada
- Consiste en un motor de búsqueda propio que permite la identificación y categorización de los distintos tipos de prendas de vestir. 
- Lo anterior con el objetivo de proporcionar una herramienta que permita a Bingo un equilibrio entre costos/funcionalidad.
### Datos
- Se realizó una búsqueda de conjuntos de imágenes de prendas de vestir etiquetadas.
- Encontramos diversos datasets pero la gran mayoría requerían de un etiquetado manual.
- Se intentó hacer uso de herramientas como Azure Vision Studio para el etiquetado automático.
- Finalmente, encontramos un dataset proveniente de la Universidad de Hong Kong llamado Large-scale Fashion (DeepFashion) Database
- **Cantidad de datos:** Aproximadamente 300.000 imágenes
- **Porcentaje usado para el entrenamiento:** 70%
- **Porcentaje usado para el testeo:** 30%

### Elección del modelo
| Criterio | Faster-RCNN | YOLOv8 |
|:--------:|:-----------:|:------:|
| Precisión (mAP) | 2-5% superior en comparación con YOLOv8 | Buena, pero inferior a Faster R-CNN |
| Velocidad de Inferencia | Más lento (20-30 fps en GPUs potentes) | Muy rápido (hasta 60-120 fps) |
| Requerimientos de Hardware | Alto (mejor en servidores potentes) | Moderado (funciona bien en hardware limitado) |
| Aplicaciones | Investigación, análisis detallado | Tiempo real, vigilancia, aplicaciones móviles |
| Facilidad de Implementación | Complejo, pero flexible y ajustable | Simple, menos flexible |
| Etiquetado | Más sencillo de encontrar | Moderado |

### Entrenamiento
- PyTorch proporciona una plataforma robusta y flexible para implementar, entrenar y optimizar modelos de detección de objetos como Faster R-CNN. La combinación de las herramientas avanzadas de PyTorch y las capacidades de detección de objetos de Faster R-CNN permitió construir el modelo.
- El modelo es capaz de diferenciar entre  46 distintas prendas de ropa.
- El modelo fue entrenado en la infraestructura de la Universidad de Los Andes con una nvidia A40 con 50 épocas y tardo 6 días en entrenar apróximadamente.

| Tipo de prenda | Tipo de prenda |
|:--------:|:------:|
| Blusa	| Halter |
| Americana |	Falda |
|Camisa de botones | Pantalones cortos |
|Bomber | Jeans |
|Anorak	| Joggers |
|Camiseta | Pantalones de chándal |
|Camiseta sin mangas | Jeggings |
|Top | Pantalones cortos tipo bermuda |
|Suéter	| Pantalones cortos deportivos |
|Franela | Leggings |
|Sudadera con capucha | Pantalones anchos |
|Cárdigan	| Chinos |
|Chaqueta	| Bañador |
|Henley	| Pareo |
|Poncho	| Pantalones estilo gauchos |
|Jersey | Jodhpurs |
|Cuello alto | Capris |
|Parka | Vestido |
|Abrigo de lana |	Monoshort |
|Abrigo |	Kimono |
|Enterizo |	Bata |
|Caftán	Monos | (Enterizo) |
|Cubierta | |

### Implementación en la aplicación móvil
- Para integrar el modelo en la aplicación móvil, se usó un API implementado en Python usando el framework FastAPI.
- Se exportó a un formato .pth para que el API pudiera usarse como intermediario entre la aplicación móvil y el modelo.
- Se creó un endpoint que recibiera imágenes de la aplicación móvil.

### Vistas
|Tipo de vista| Vista |
|:--------:|:------:|
| Vista inicial de la aplicación | <img width="283" alt="Screenshot 2024-07-23 at 2 18 33 PM" src="https://github.com/user-attachments/assets/61e89c20-b9c2-4371-bcbf-b7fc6247d57e"> |
| Pantalla principal | <img width="280" alt="Screenshot 2024-07-23 at 2 22 59 PM" src="https://github.com/user-attachments/assets/314622c0-979e-47aa-9155-a01fb0622906"> |
| Vista inicial de la funcionalidad Búsqueda Avanzada| <img width="248" alt="Screenshot 2024-07-23 at 2 20 25 PM" src="https://github.com/user-attachments/assets/1a49d255-d18a-43a7-b9d9-198305c53784"> |
| Subir foto de la galeria del celular | <img width="277" alt="Screenshot 2024-07-23 at 2 24 32 PM" src="https://github.com/user-attachments/assets/0f9e5312-f281-4b16-a3d2-434d5667ec63"> |
| Analizar foto escogida | <img width="262" alt="Screenshot 2024-07-23 at 2 26 56 PM" src="https://github.com/user-attachments/assets/87f6627d-af50-4776-af79-b6f37e52d2c2"> |
| Identificación de la prenda | <img width="257" alt="Screenshot 2024-07-23 at 2 52 12 PM" src="https://github.com/user-attachments/assets/efc56674-20ea-4bc4-b9cc-a4e0f0f6159d"> | 


## Motor de medidas
Se desarrolló la implementación y prueba de un aplicativo que permite la detección de diferentes medidas relevantes del cuerpo humano por medio de imágenes. Lo anterior, con el objetivo de mejorar la experiencia de usuario a la hora de realizar compras por medio de sugerencias basadas en las medidas detectadas.
### Dos aproximaciones
- **2D:** Consiste en hacer detección de puntos clave del cuerpo humano y en base a una medida estándar recibida por input realizar las mediciones.
- **3D:** Utiliza el modelo SMPL, que es un modelo 3D paramétrico del cuerpo humano. El modelo SMPL representa el cuerpo humano utilizando un conjunto de parámetros que incluyen la forma (shape) y la pose.
### Características
Este modelo utiliza técnicas de aprendizaje profundo y el modelo SMPL para estimar la forma y pose 3D del cuerpo humano a partir de una imagen 2D. Luego, utiliza los vértices del modelo SMPL y puntos de control predefinidos para calcular medidas corporales específicas. La forma se representa mediante 10 coeficientes de forma y la pose se representa mediante 72 parámetros de rotación para 24 articulaciones.

**Repositorio:** https://github.com/farazBhatti/Human-Body-Measurements-using-Computer-Vision/tree/master

### Funcionamiento
<img width="376" alt="Screenshot 2024-07-23 at 2 07 44 PM" src="https://github.com/user-attachments/assets/e4adb6aa-8f09-4cf5-a6a4-aa8cc076b86b">

Se toman medidas de:
- Altura
- Cintura
- Barriga
- Pecho
- Muñeca
- Cuello
- Longitud del brazo
- Muslo
- Ancho de hombros
- Caderas
- Tobillo
### Implementación en la aplicación móvil
- Para integrar el modelo en la aplicación móvil, se usó un API implementado en Python usando el framework FastAPI.
- Se expuso y configuró un endpoint que recibe la imagen y la altura del usuario como input.
### Vistas
|Tipo de vista| Vista |
|:--------:|:------:|
| Vista inicial de la funcionalidad Motor de medidas donde se ingresa la altura| <img width="179" alt="Screenshot 2024-07-23 at 2 56 13 PM" src="https://github.com/user-attachments/assets/453755fe-2fe7-4b46-8bfb-2ad2eb094147"> |
| Vista para tomar la foto | <img width="234" alt="Screenshot 2024-07-23 at 2 57 48 PM" src="https://github.com/user-attachments/assets/cba12493-fdad-4bc8-baad-2701175d780c"> |
| Resultados | <img width="172" alt="Screenshot 2024-07-23 at 2 59 45 PM" src="https://github.com/user-attachments/assets/3795e019-1609-4fce-8863-34ce44185f40"> |
