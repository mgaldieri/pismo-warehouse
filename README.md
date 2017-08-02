# Pismo Warehouse

#### Introdução

Esse aplicativo simula um sistema de controle de estoque fictício para uma loja.
Toda a comunicação é feita através de interface REST utilizando dados em JSON e autenticação por header HTTP.

O aplicativo foi desenvolvido em [Kotlin][kotlinlang.org] e roda sobre JVM para Java 8, enquanto os testes foram
desenvolvidos em Python 3. A execução do aplicativo pode ser feito de duas maneiras:
* Compilação do código através do Gradle, utilizando o script `runserver.sh`
* Em um container Docker, através do script `rundocker.sh`

Da mesma maneira, para encerrar o aplicativo pode-se utilizar os scripts `stopserver.sh` e `stopdocker.sh`, respectivamente.

Os testes são realizados utilizando a versão Docker e são executados pelo script `runtests.sh`, que instala as dependências
do Python automaticamente em um virtualenv que é removido após o término destes.

#### Especificações

Existem dois tipos de endpoints disponíveis: um para um usuário administrador (admin) e outro para interface com
a loja fictícia [Pismo Store][https://github.com/mgaldieri/pismo-store] (vendor).

Todas as respostas do servidor seguem o mesmo padrão:
```
{
    "data": <object, nullable>,
    "error": <object, nullable>
}
```
Sendo que o campo `data` conterá os dados requisitados (nulo quando não houver dados) e o campo `error` a mensagem de erro específica, quando houver
(nulo quando não houver erros)

As mensagens de erro, por sua vez seguem o modelo:
```
{
    "type": <string>,       // Tipo de error (default: "Server error")
    "errorCode": <string>,  // Código interno de erro (default: "0000")
    "httpCode": <int>,      // Status HTTP (padrão RFC7231)
    "message": <string>     // Mensagem descritiva
}
```

Para testar esses endpoints as credenciais são:

*Usuário administrador*
- email: `admin@email.com`
- senha: `admin123`

*Usuário Pismo Store*
- api-key: `2289edc82ff62d5d8a82ad2ef7079871aef71713`

##### Autenticação

*Usuário administrador*

Inicialmente o adminsitrador deverá logar no sistema:


`[POST] admin/login`
```
REQUEST BODY:
{
    "email": <string>,
    "password": <string>
}

RESPONSE:
{
    "data": {
        "jwt": <string>
    },
    "error": <string, nullable>
}
```

O campo de retorno `jwt` conterá um token que identifica o usuário no sistema, e deverá ser inserido no header
`Authentication`, com o valor `Bearer <TOKEN>` para todos os acessos subsequentes.

*Usuário Pismo Store*

Parte-se do principio de que o usuário Pismo Store é um aplicativo previamente cadastrado no sistema e já possui um token
de acesso, portanto esse token deve simplesmente ser inserido no header `Authentication` com o valor `<API-KEY>` para todas as chamadas.

##### Endpoints ADMIN

`[POST] admin/logout`

Realiza a desautenticação do usuário no sistema.

```
REQUEST BODY:
N/A

RESPONSE:
{
    "data": <null>,
    "error": <null>
}
```
---

`[GET] admin/products`

Lista todos os produtos cadastrados (alguns já estão previamente cadastrados no sistema)

```
REQUEST BODY:
N/A

RESPONSE:
{
    "data": {
        "products": [
            {
                "id": <int>,                // Id do produto no sistema
                "name": <string>,           // Nome do produto
                "description": <string>,    // Descrição do produto
                "priceCents": <int>,        // Preço do produto, em centavos
                "qty": <int>                // Quantidade do produto em estoque
            },...
        ]
    },
    "error": <null>
}
```

---

`[GET] admin/product/<productId: int>`

Lista as informações do produto com o id `productId`

```
REQUEST BODY:
N/A

RESPONSE:
{
    "data": {
        "product": {
            "id": <int>,                // Id do produto no sistema
            "name": <string>,           // Nome do produto
            "description": <string>,    // Descrição do produto
            "priceCents": <int>,        // Preço do produto, em centavos
            "qty": <int>                // Quantidade do produto em estoque
        }
    },
    "error": <null>
}
```

---

`[PUT] admin/product`

Insere um novo produto no sistema

```
REQUEST BODY:
{
    "name": <string>,           // Nome do produto
    "description": <string>,    // Descrição do produto
    "priceCents": <int>,        // Preço do produto, em centavos
    "qty": <int>                // Quantidade do produto em estoque
}

RESPONSE:
{
    "data": {
        "productId": <int>      // Id do produto cadastrado no sistema
    },
    "error": <null>
}
```

---

`[PUT] admin/product/<productId: int>`

Adiciona mais unidades do produto com o id `productId` ao estoque

```
REQUEST BODY:
{
    "qty": <int>    // A quantidade do produto a ser inserida
}

RESPONSE:
{
    "data": <null>,
    "error": <null>
}
```

---

`[POST] admin/product/<productId: int>`

Altera os dados de um produto no estoque com o id `productId`

```
REQUEST BODY:
{
    "name": <string, nullable>,             // Nome do produto
    "description": <string, nullable>,      // Descrição do produto
    "priceCents": <int, nullable>,          // Preço do produto, em centavos
    "qty": <int, nullable>                  // Quantidade do produto em estoque
}

RESPONSE:
{
    "data": <null>,
    "error": <null>
}
```

---

##### Endpoints VENDOR

`[GET] vendor/products`

Lista todos os produtos cadastrados

```
REQUEST BODY:
N/A

RESPONSE:
{
    "data": {
        "products": [
            {
                "id": <int>,                // Id do produto no sistema
                "name": <string>,           // Nome do produto
                "description": <string>,    // Descrição do produto
                "priceCents": <int>,        // Preço do produto, em centavos
                "qty": <int>                // Quantidade do produto em estoque
            },...
        ]
    },
    "error": <null>
}
```

---

`[GET] vendor/product/<productId: int>`

Lista as informações do produto com o id `productId`

```
REQUEST BODY:
N/A

RESPONSE:
{
    "data": {
        "product": {
            "id": <int>,                // Id do produto no sistema
            "name": <string>,           // Nome do produto
            "description": <string>,    // Descrição do produto
            "priceCents": <int>,        // Preço do produto, em centavos
            "qty": <int>                // Quantidade do produto em estoque
        }
    },
    "error": <null>
}
```

---

`[GET] vendor/product/<productId: int>/qty`

Retorna a quantidade disponível em estoque para o produto com o id `productId`

```
REQUEST BODY:
N/A

RESPONSE:
{
    "data": {
        "qty": <int>    // Quantidade do produto em estoque
    },
    "error": <null>
}
```

---

`[GET] vendor/products/search?term=<namePart>`

Lista todos os produtos cadastrados cujo nome contenha o texto `namePart`

```
REQUEST BODY:
N/A

RESPONSE:
{
    "data": {
        "products": [
            {
                "id": <int>,                // Id do produto no sistema
                "name": <string>,           // Nome do produto
                "description": <string>,    // Descrição do produto
                "priceCents": <int>,        // Preço do produto, em centavos
                "qty": <int>                // Quantidade do produto em estoque
            },...
        ]
    },
    "error": <null>
}
```

---

`[POST] vendor/products/sell`

Realiza a venda de produtos e registra tanto as transações como a alteração da quantidade disponível em estoque

```
REQUEST BODY:
{
    `products`: [
        {
            "productId": <int>  // Id do produto a ser vendido
            "qty": <int>        // Quantidade do produto a ser vendido
        },...
    ]
}

RESPONSE:
{
    "data": <null>,
    "error": <null>,
}
```