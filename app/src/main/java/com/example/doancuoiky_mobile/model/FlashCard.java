package com.example.doancuoiky_mobile.model;

public class FlashCard {

    private String englishWord;     // Từ tiếng Anh
    private String vietnameseWord;  // Nghĩa tiếng Việt
    private String imageUrl;
    private boolean isFlipped;

    public FlashCard() {}

    public FlashCard(String englishWord, String vietnameseWord, String imageUrl) {
        this.englishWord = englishWord;
        this.vietnameseWord = vietnameseWord;
        this.imageUrl = imageUrl;
    }

    public String getEnglishWord() {
        return englishWord;
    }

    public void setEnglishWord(String englishWord) {
        this.englishWord = englishWord;
    }

    public String getVietnameseWord() {
        return vietnameseWord;
    }

    public void setVietnameseWord(String vietnameseWord) {
        this.vietnameseWord = vietnameseWord;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isFlipped() {
        return isFlipped;
    }

    public void setFlipped(boolean flipped) {
        isFlipped = flipped;
    }
}

