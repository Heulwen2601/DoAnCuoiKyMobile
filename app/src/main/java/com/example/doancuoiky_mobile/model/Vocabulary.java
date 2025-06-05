package com.example.doancuoiky_mobile.model;

public class Vocabulary {

    private String englishWord;     // Từ tiếng Anh
    private String vietnameseWord;  // Nghĩa tiếng Việt
    private String status;      // Trạng thái học ("not_learned", "learning", "mastered")
    private boolean isStarred;  // Đánh dấu sao
    private String pronunciation; // Chuẩn phát âm (tùy chọn)

    // Constructor, getters và setters
    public Vocabulary() {}

    public Vocabulary(String english, String vietnamese, String description, String imageUrl, String status, boolean isStarred, String pronunciation) {
        this.englishWord = english;
        this.vietnameseWord = vietnamese;
        this.status = status;
        this.isStarred = isStarred;
        this.pronunciation = pronunciation;
    }

    public String getEnglishWord() {
        return englishWord;
    }

    public void setEnglishWord(String english) {
        this.englishWord = english;
    }

    public String getVietnameseWord() {
        return vietnameseWord;
    }

    public void setVietnameseWord(String vietnamese) {
        this.vietnameseWord = vietnamese;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public String getStatus() {
        return this.status;
    }



    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean starred) {
        isStarred = starred;
    }

    public String getPronunciation() {
        return pronunciation != null ? pronunciation : englishWord; // Trả về pronunciation nếu có, nếu không thì dùng english
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }
}
