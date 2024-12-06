# springragpdf
This application allows users to upload PDF documents, generate embeddings using Ollama's Llama 3.1 model, store these embeddings in a PostgreSQL database with PGVector, and then query the documents based on those embeddings. Users can ask questions related to the content of the uploaded PDFs, and the system will return relevant answers based on the stored embeddings.

Tech Stack
Ollama (Llama 3.1): A large language model used to generate embeddings for the content of the uploaded PDFs.
PostgreSQL with PGVector: A PostgreSQL extension to store and query vector embeddings.
Spring AI: A Spring-based framework to build the backend and manage the application's logic.
Features
Upload PDF: Users can upload a PDF document to the system.
Generate Embeddings: The content of the PDF is processed, and embeddings are generated using the Ollama Llama 3.1 model.
Store Embeddings: The embeddings are stored in a PostgreSQL database using the PGVector extension.
Ask Questions: Users can ask questions based on the uploaded document, and the system will return the most relevant content by comparing the question's embedding with the stored document embeddings.
