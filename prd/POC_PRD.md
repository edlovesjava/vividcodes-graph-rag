The tool should have the following capabilities

1. read code from multiple repositories
   1. initially from file location,
   2. filtering for java source
2. parse and transform Java code into LPG (Labelled Property Graph)
   1. identify key components of Java such as packages, classes, methods, as well as key relationships such as containment and dependencies
   2. but limited to public classes, methods, etc.
3. ingest into graph db container with references to actual code and line numbers as the source document
4. include model that can parse prompts into graph queries to gain context
5. output as links to code to augment the prompt with context
6. provide a tool to retrieve referenced code chunks from source to use as specific context
7. run prompt woith context on LLM

see [Technical Specifications](./TECHNICAL_SPECIFICATION.md)
