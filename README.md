# Shape Sentinel

Projeto de visão computacional para detecção de formas geométricas em tempo real, combinando Java 17, OpenCV e uma interface web voltada à captura e visualização pela câmera.

O objetivo é transformar quadros de vídeo em informação geométrica útil, organizando a detecção de contornos e formas em uma aplicação prática, testável e fácil de evoluir.

## Destaques

- Processamento de imagem em tempo real com OpenCV.
- Detecção e classificação de formas a partir de contornos geométricos.
- Captura por câmera com interface web para acompanhamento visual.
- Estrutura Java organizada com Maven.
- Build empacotado em JAR executável.
- Testes automatizados com JUnit.

O projeto funciona como uma base para experimentar visão computacional clássica, processamento de imagem e integração entre componentes Java e uma camada visual no navegador.

## Stack

- **Java 17+**
- **OpenCV 4.9**
- **Maven 3.9+**
- **JUnit 5**
- HTML, CSS e JavaScript para a experiência visual

## Build

```bash
mvn clean package
```

O Maven Shade Plugin gera um artefato executável com a classe principal `ShapeSentinel`, simplificando distribuição e execução do projeto.

## Valor técnico

Shape Sentinel foi estruturado para demonstrar fundamentos de computer vision de forma prática: captura, interpretação de contornos, classificação geométrica e apresentação dos resultados em tempo real. É uma base adequada para evoluções futuras em reconhecimento visual, automação e análise de imagens.
