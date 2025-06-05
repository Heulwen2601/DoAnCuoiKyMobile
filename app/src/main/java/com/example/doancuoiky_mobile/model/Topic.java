package com.example.doancuoiky_mobile.model;

import java.util.ArrayList;
import java.util.List;

public class Topic {

    private String name;
    private String description;
    private String privacy;
    private List<Vocabulary> vocabularies; // Danh sách Vocabulary
    private List<FlashCard> flashCards;    // Danh sách FlashCard

    public Topic() {}

    public Topic(String name, String description, String privacy, List<Vocabulary> vocabularies, List<FlashCard> flashCards) {
        this.name = name;
        this.description = description;
        this.privacy = privacy;
        this.vocabularies = vocabularies;
        this.flashCards = flashCards;
    }

    public String getName() {
        return name;
    }
    public String getPrivacy() {return privacy;}
    public void setPrivacy(String privacy) {this.privacy = privacy;}

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Vocabulary> getVocabularies() {
        return vocabularies;
    }

    public void setVocabularies(List<Vocabulary> vocabularies) {
        this.vocabularies = vocabularies;
    }

    public List<FlashCard> getFlashCards() {
        return flashCards;
    }

    public void addFlashCard(FlashCard flashCard) {
        if (this.flashCards == null) {
            this.flashCards = new ArrayList<>();
        }
        this.flashCards.add(flashCard);
    }



    public int getVocabularyCount() {
        return vocabularies != null ? vocabularies.size() : 0;  // Trả về số lượng từ vựng
    }

    public void setVocabularyCount(int vocabularyCount) {
        // Thực tế không cần phương thức này nếu bạn đang dùng List<String> vocabularies
    }
}
