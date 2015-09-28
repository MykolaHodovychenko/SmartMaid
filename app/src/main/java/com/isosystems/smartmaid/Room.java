package com.isosystems.smartmaid;

import com.isosystems.smartmaid.utils.RoomsManager;

import java.util.Random;

public class Room {

    public int room_number;
    public boolean maid; // Вызов горничной
    public boolean clean; // Уборка
    public boolean wash; // Стирка
    public boolean dnd; // Не беспокоить
    public boolean minibar; //Пополнение мини-бара

    public boolean valueChanged;

    public Room (int number) {
        room_number = number;
        maid = false;
        clean = false;
        wash = false;
        dnd = false;
        minibar = false;
        valueChanged = false;
    }

    public Room (int number,
                 boolean dnd,
                 boolean maid,
                 boolean clean,
                 boolean wash,
                 boolean minibar) {
        this.room_number = number;
        this.dnd = dnd;
        this.maid = maid;
        this.clean = clean;
        this.wash = wash;
        this.minibar = minibar;
        this.valueChanged = true;
    }

    public int compareTo(Room anotherInstance, RoomsManager.SortingField field) {
        switch (field) {
            case ROOM:
                return this.room_number - anotherInstance.room_number;
            case MAID:
                int a = (this.maid) ? 1 : 0;
                int b = (anotherInstance.maid) ? 1 : 0;
                return -(a - b);
            case CLEAN:
                a = (this.clean) ? 1 : 0;
                b = (anotherInstance.clean) ? 1 : 0;
                return -(a - b);
            case DND:
                a = (this.dnd) ? 1 : 0;
                b = (anotherInstance.dnd) ? 1 : 0;
                return -(a - b);
            case WASH:
                a = (this.wash) ? 1 : 0;
                b = (anotherInstance.wash) ? 1 : 0;
                return -(a - b);
            case MINIBAR:
                a = (this.minibar) ? 1 : 0;
                b = (anotherInstance.minibar) ? 1 : 0;
                return -(a - b);
        }
        return 0;
    }

    public enum CompareField {
        ROOM,
        MAID,
        CLEAN,
        DND,
        WASH,
        MINIBAR
    }
}
