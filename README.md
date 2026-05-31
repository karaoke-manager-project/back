# Como rodar o projeto:
> [!NOTE]
> Atenção! Sempre que eu utilizar um comando "docker" seu comando é igual no podman com a diferença apenas no nome do comando.
> Caso você deseje rodar o podman com o nome docker você pode criar um alias ou baixar o podman-docker (no archlinux)

## Pré requisitos
  1. É necessário ter o Java 17 instalado
  2. É necessário utilizar docker ou podman
  3. É necessário criar um .env que siga o exemplo de .env-example

## Como rodar

Para inicializar o postgres e o redis é necessário rodar o seguinte comando:
```bash
docker compose up -d
```
Agora sempre que for iniciar o projeto você roda apenas o comando
```bash
./rundev
```


## Troubleshooting

Caso no desenvolvimento seja necessário resetar os valores de um container redis e os valores de um container postgres você roda os comando
```bash
docker compose down -v
docker compose up -d
```
E ai você tem todos os valores reiniciados.

Caso você precise reiniciar os valores de apenas um específico (como por exemplo: o redis) voce deve fazer
```bash
docker compose down
docker volume rm redis_data
docker compose up -d
```

