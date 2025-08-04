Tive interesse em fazer uma API RESTful para assinar documentos digitais com validade jurídica. Para isso o usuário necessita de uma assinatura do tipo A1, que é um documento digital
utilizado para esse tipo de assinatura, você pode encontrar uma para comprar no site oficial do governo como e-CPF para pessoas físicas, mas tem um custo.
Pretendo comprar no futuro para mais testes, mas no momento a API está disponível para assinaturas utilizando um par de keys RSA localmente, por tanto, a assinatura é inválida.
Você pode verificar a assinatura realizada utilizando leitores PDF que tenham essa ferramenta(Ex. Adobe Reader).

O que foi feito até agora na API 29/07/2025, foram apenas testes, sem tratamento de erros para ver a viabilidade do projeto.

Em breve disponibilizarei a collection que utilizo no postman para testes, para os interessados.

###########################
Como rodar o projet: 
  Requisitos: 
    - Ter Python 3 no seu pc.
    - Ter Docker instalado.
Com isso baixado váaté a pasta start-script, tem um codigo python. Basta rodar ele, para executar vá até a pasta que ele esta, abra o shell e digite `python start.py`.
É esperado abrir um terminal que vai instalar as dependencias necessarias, após isso vai aparecer o start do do FrontEnd em react e do BackEnd com java Spring Boot.
Após a conclusão dos logs, vocë pode ir até seu navegador e acessar a url: "http://localhost:5173", vocë pode enviar um arquivo pdf para assinatura.
