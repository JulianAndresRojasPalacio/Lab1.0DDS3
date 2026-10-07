package Model;

public class Card {
    private String name;
    private int atk;
    private int def;
    private String imageUrl;
    private String position; // "attack" o "defense"

    public Card(String name, int atk, int def, String imageUrl) {
        this.name = name;
        this.atk = atk;
        this.def = def;
        this.imageUrl = imageUrl;
        this.position = "attack"; // default
    }

    // Getters y setters
    public String getName() { return name; }
    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public String getImageUrl() { return imageUrl; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}