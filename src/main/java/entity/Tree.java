package entity;

import javax.persistence.*;

@Entity
@Table(name = "Tree")

public class Tree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    private Long id;

    private Integer left;

    private Integer right;

    private Integer level;

    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLeft() {
        return left;
    }

    public void setLeft(Integer left) {
        this.left = left;
    }

    public Integer getRight() {
        return right;
    }

    public void setRight(Integer right) {
        this.right = right;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
