package ma.emsi.gounidimeryem.tp2web_gounidimeryem.jsf;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.util.Properties;

@ApplicationScoped
public class LlmClientGemini {
    // Interface pour les interactions LLM
    private Assistant assistant;

    // Mémoire de l'assistant pour garder l'historique de la conversation
    private ChatMemory chatMemory;
    
    // Rôle système actuel
    private String systemRole;

    public LlmClientGemini() {
        Properties props = new Properties();
        try {
            // Chargement de la clé depuis le fichier properties
            props.load(LlmClientGemini.class.getResourceAsStream("/chat.properties"));
            String apiKey = props.getProperty("GeminiKey");
            
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("La clé GeminiKey n'est pas définie dans chat.properties!");
            }

            // Création du modèle Gemini
            ChatModel modele = GoogleAiGeminiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName("gemini-pro")
                    .build();

            // Initialisation de la mémoire de chat (10 derniers messages)
            this.chatMemory = MessageWindowChatMemory.withMaxMessages(10);

            // Construction de l'assistant avec le modèle et la mémoire
            this.assistant = AiServices.builder(Assistant.class)
                    .chatModel(modele)
                    .chatMemory(chatMemory)
                    .build();
                    
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger chat.properties", e);
        }
    }

    public void setSystemRole(String systemRole) {
        // Réinitialisation de la conversation
        this.chatMemory.clear();
        
        this.systemRole = systemRole;

        // Ajout du nouveau rôle système
        if (systemRole != null && !systemRole.isEmpty()) {
            this.chatMemory.add(SystemMessage.from(systemRole));
        }
    }

    public String chat(String question) {
        return this.assistant.chat(question);
    }
}