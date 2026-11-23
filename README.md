# Sistema de Fórmulas de Produtos

Aplicação local para cadastrar produtos e suas fórmulas, organizar componentes em uma estrutura de árvore e calcular o custo total a partir das quantidades e dos valores unitários.

O projeto foi desenvolvido em Java, com uma interface web leve em HTML, CSS e JavaScript, um servidor HTTP integrado ao Java e persistência local em SQLite.

## O que o sistema faz

- Cadastra um produto raiz e seus componentes.
- Relaciona cada componente ao produto ou componente pai.
- Registra quantidade e valor unitário.
- Exibe a composição hierárquica da fórmula.
- Calcula o custo para uma quantidade informada de produtos.
- Permite atualizar o valor de um componente e exportar a fórmula como texto.

O cálculo considera a quantidade de cada item na árvore multiplicada pelo seu valor unitário e soma os resultados.

## Tecnologias

- Java (JDK 17 ou superior recomendado)
- SQLite
- SQLite JDBC (`sqlite-jdbc` 3.51.0.0)
- HTML, CSS e JavaScript

## Como executar no Windows

1. Instale um JDK. O comando `javac` precisa estar disponível no terminal.
2. Abra esta pasta no VS Code.
3. No terminal integrado, confirme que ele está na pasta do projeto.
4. Compile os arquivos Java:

```powershell
javac -cp ".;lib/sqlite-jdbc-3.51.0.0.jar" ApiServer.java FormulaTree.java Database.java FormulaService.java ItemFormula.java TestMain.java
```

5. Inicie o servidor:

```powershell
java -cp ".;lib/sqlite-jdbc-3.51.0.0.jar" ApiServer
```

6. Abra <http://localhost:8050> no navegador.

Também é possível executar `iniciar.bat`, que compila os arquivos e inicia o servidor.

Se a porta `8050` já estiver ocupada, informe outra porta ao iniciar, por exemplo:

```powershell
java -cp ".;lib/sqlite-jdbc-3.51.0.0.jar" ApiServer 8051
```

O banco `produto.db` é criado localmente na pasta do projeto quando o servidor inicia. Ele não é enviado ao GitHub; cada pessoa terá seu próprio banco local.

## Estrutura

```text
.
├── ApiServer.java
├── FormulaTree.java
├── Database.java
├── FormulaService.java
├── ItemFormula.java
├── TestMain.java
├── iniciar.bat
├── lib/
│   ├── sqlite-jdbc-3.51.0.0.jar
│   └── LICENSE-sqlite-jdbc.txt
└── web/
    ├── index.html
    ├── script.js
    └── style.css
```

## Observações

- O servidor usa a porta `8050`. Se ela já estiver ocupada, feche a outra aplicação que a está usando antes de iniciar este projeto.
- Os arquivos `.class` são gerados durante a compilação e não precisam ser versionados.
- O banco local e os arquivos temporários estão excluídos do Git.
- O código deste projeto ainda não declara uma licença de código aberto. A licença do driver SQLite é separada e está em `lib/LICENSE-sqlite-jdbc.txt`.

## Autoria

Projeto acadêmico desenvolvido por Caio Caso.
