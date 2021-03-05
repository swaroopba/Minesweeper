package com.example.sample;

public class Cell {
    Integer cellContent;
    Boolean isOpen;

    public Cell()
    {
        cellContent = 0;
        isOpen = false;
    }

    public Cell(Integer cellContent, Boolean isOpen)
    {
        this.cellContent = cellContent;
        this.isOpen = isOpen;
    }

    public Integer getCellContent() {
        return cellContent;
    }

    public Boolean getOpen() {
        return isOpen;
    }

    public void setCellContent(Integer cellContent) {
        this.cellContent = cellContent;
    }

    public void setOpen(Boolean open) {
        isOpen = open;
    }
}
