package com.isosystems.smartmaid.utils;


import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.isosystems.smartmaid.ListAdapter;
import com.isosystems.smartmaid.MyApplication;
import com.isosystems.smartmaid.R;
import com.isosystems.smartmaid.Room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RoomsManager implements View.OnClickListener {

    // Массив с номерами
    ArrayList<Room> roomsArray;

    boolean mShowEmptyRooms;

    Context mContext;

    // Стартовый номер
    int mStartingRoomNumber = 100;
    // Количество номеров
    int mNumberOfRooms = 50;

    // Кнопки для сортировки
    // Кнопки DND
    FlipMenuButton mLeftDNDSortButton;
    FlipMenuButton mRightDNDSortButton;
    // Кнопки вызова горничной
    FlipMenuButton mLeftMaidSortButton;
    FlipMenuButton mRightMaidSortButton;
    // Кнопки уборки
    FlipMenuButton mLeftCleanSortButton;
    FlipMenuButton mRightCleanSortButton;
    // Кнопки стирки
    FlipMenuButton mLeftWashSortButton;
    FlipMenuButton mRightWashSortButton;
    // Кнопки минибара
    FlipMenuButton mLeftMinibarSortButton;
    FlipMenuButton mRightMinibarSortButton;

    // Левая часть комнат (без пустых)
    List<Room> mSelectiveLeftRoomsArray;
    // Правая часть комнат (без пустых)
    List<Room> mSelectiveRightRoomsArray;
    // Левая часть комнат (полностью)
    List<Room> mFullLeftRoomsArray;
    // Правая часть комнат (полностью)
    List<Room> mFullRightRoomsArray;

    // левый ListView
    ListView mLeftListView;
    // правый ListView
    ListView mRightListView;

    // Адаптер для левого ListView
    ListAdapter mLeftListViewAdapter;
    // Адаптер для правого ListView
    ListAdapter mRightListViewAdapter;

    // View, на котором расположены кнопки
    View layoutView;

    // Поле, по которому осуществляется сортировка
    SortingField sortingField = SortingField.ROOM;

    // Конструктор
    public RoomsManager(View view, Context context, int starting_number, int numbers_count) {
        this.layoutView = view;

        this.mContext = context;

        // Установка стартового номера
        this.mStartingRoomNumber = starting_number;
        // Установка количества номеров
        this.mNumberOfRooms = numbers_count;

        // Создается массив с номерами
        roomsArray = new ArrayList<Room>();
        // Инициализация массива с номерами
        populateRoomsArray();

        // Инициализация и установка слушателей для кнопок сортировки
        setSortButtons();

        // Инициализация массивов для адаптеров
        mSelectiveLeftRoomsArray = new ArrayList<Room>();
        mSelectiveRightRoomsArray = new ArrayList<Room>();
        mFullLeftRoomsArray = roomsArray.subList(0, roomsArray.size() / 2);
        mFullRightRoomsArray = roomsArray.subList(roomsArray.size() / 2, roomsArray.size());

        // Инициализация адаптеров
        mLeftListViewAdapter = new ListAdapter(mFullLeftRoomsArray, context);
        mRightListViewAdapter = new ListAdapter(mFullRightRoomsArray, context);

        mLeftListView = (ListView) layoutView.findViewById(R.id.list);
        mLeftListView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                view.clearAnimation();
            }
        });
        mRightListView = (ListView) layoutView.findViewById(R.id.list_2);
        mRightListView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                view.clearAnimation();
            }
        });
        mLeftListView.setAdapter(mLeftListViewAdapter);
        mRightListView.setAdapter(mRightListViewAdapter);

        //Сборка массивов без пустых номеров
        rebuildDataLists();
        updateList();
    }

    /**
     * Инициализация массива с номерами.
     * В цикле в массив добавляются новые объекты, происходит их инициализация,
     * заполняются номера комнат
     */
    private void populateRoomsArray() {
        for (int i = 0; i < mNumberOfRooms; i++) {
            roomsArray.add(new Room(mStartingRoomNumber + i));
        }
    }

    /**
     * Инициализация кнопок сортировки и установка слушателей
     */
    private void setSortButtons() {
        // DND
        mLeftDNDSortButton = (FlipMenuButton) layoutView.findViewById(R.id.left_dnd_flip);
        mLeftDNDSortButton.setImageResources(R.drawable.dnd, R.drawable.dnd_active);
        mLeftDNDSortButton.setOnClickListener(this);

        mRightDNDSortButton = (FlipMenuButton) layoutView.findViewById(R.id.right_dnd_flip);
        mRightDNDSortButton.setImageResources(R.drawable.dnd, R.drawable.dnd_active);
        mRightDNDSortButton.setOnClickListener(this);

        // MAID
        mLeftMaidSortButton = (FlipMenuButton) layoutView.findViewById(R.id.left_maid_flip);
        mLeftMaidSortButton.setImageResources(R.drawable.maid, R.drawable.maid_active);
        mLeftMaidSortButton.setOnClickListener(this);

        mRightMaidSortButton = (FlipMenuButton) layoutView.findViewById(R.id.right_maid_flip);
        mRightMaidSortButton.setImageResources(R.drawable.maid, R.drawable.maid_active);
        mRightMaidSortButton.setOnClickListener(this);

        // CLEAN
        mLeftCleanSortButton = (FlipMenuButton) layoutView.findViewById(R.id.left_clean_flip);
        mLeftCleanSortButton.setImageResources(R.drawable.clean, R.drawable.clean_active);
        mLeftCleanSortButton.setOnClickListener(this);

        mRightCleanSortButton = (FlipMenuButton) layoutView.findViewById(R.id.right_clean_flip);
        mRightCleanSortButton.setImageResources(R.drawable.clean, R.drawable.clean_active);
        mRightCleanSortButton.setOnClickListener(this);

        // WASH
        mLeftWashSortButton = (FlipMenuButton) layoutView.findViewById(R.id.left_wash_flip);
        mLeftWashSortButton.setImageResources(R.drawable.wash, R.drawable.wash_active);
        mLeftWashSortButton.setOnClickListener(this);

        mRightWashSortButton = (FlipMenuButton) layoutView.findViewById(R.id.right_wash_flip);
        mRightWashSortButton.setImageResources(R.drawable.wash, R.drawable.wash_active);
        mRightWashSortButton.setOnClickListener(this);

        // MINIBAR
        mLeftMinibarSortButton = (FlipMenuButton) layoutView.findViewById(R.id.left_minibar_flip);
        mLeftMinibarSortButton.setImageResources(R.drawable.minibar, R.drawable.minibar_active);
        mLeftMinibarSortButton.setOnClickListener(this);

        mRightMinibarSortButton = (FlipMenuButton) layoutView.findViewById(R.id.right_minibar_flip);
        mRightMinibarSortButton.setImageResources(R.drawable.minibar, R.drawable.minibar_active);
        mRightMinibarSortButton.setOnClickListener(this);
    }

    /**
     * Данный метод проходит по массиву номеров, ищет не пустые
     * номера и добавляет их в массивы для данных без пустых номеров
     */
    synchronized private void rebuildDataLists() {
        mSelectiveLeftRoomsArray.clear();
        mSelectiveRightRoomsArray.clear();

        for (Room r : roomsArray) {
            if (r.dnd || r.maid || r.clean || r.wash || r.minibar) {
                if (r.room_number > roomsArray.size() / 2 + mStartingRoomNumber - 1) {
                    mSelectiveRightRoomsArray.add(r);
                } else {
                    mSelectiveLeftRoomsArray.add(r);
                }
            }
        }
    }

    /**
     * Обновление данных для комнаты
     *
     * @param message новый объект для комнаты
     */
    public void updateData(String message) {

        message = message.substring(2, message.length());
        String[] array = message.split(",");

        int room_number = 0;
        try {
            room_number = Integer.parseInt(array[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // DND
        int i_dnd = 0;
        try {
            i_dnd = Integer.parseInt(array[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean dnd = (i_dnd > 0) ? Boolean.TRUE : Boolean.FALSE;

        // MAID
        int i_maid = 0;
        try {
            i_maid = Integer.parseInt(array[2]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean maid = (i_maid > 0) ? Boolean.TRUE : Boolean.FALSE;

        // CLEAN
        int i_clean = 0;
        try {
            i_clean = Integer.parseInt(array[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean clean = (i_clean > 0) ? Boolean.TRUE : Boolean.FALSE;

        // WASH
        int i_wash = 0;
        try {
            i_wash = Integer.parseInt(array[4]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean wash = (i_wash > 0) ? Boolean.TRUE : Boolean.FALSE;

        // MINIBAR
        int i_minibar = 0;
        try {
            i_minibar = Integer.parseInt(array[5]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean minibar = (i_minibar > 0) ? Boolean.TRUE : Boolean.FALSE;

        Room updated_room = new Room(room_number, dnd, maid, clean, wash, minibar);
        if (updateRoom(updated_room)) {
            sortRooms();
            rebuildDataLists();
            updateList();

            if (sortingField == SortingField.ROOM) {
                if (room_number - mStartingRoomNumber> mNumberOfRooms / 2) {
                    mRightListView.smoothScrollToPosition(room_number - mStartingRoomNumber - mNumberOfRooms / 2);
                    //Toast.makeText(MainActivity.this, String.valueOf(room_number) + " - " + String.valueOf(mNumberOfRooms/2),Toast.LENGTH_SHORT).show();
                } else {
                    mLeftListView.smoothScrollToPosition(room_number - mStartingRoomNumber);
                    //Toast.makeText(MainActivity.this, String.valueOf(room_number),Toast.LENGTH_SHORT).show();

                }
            }
            ((MyApplication) mContext.getApplicationContext()).soundMessages.playAlarmSound();
        }
    }

    private boolean updateRoom(Room room) {
        for (int i = 0; i < roomsArray.size(); i++) {
            if (roomsArray.get(i).room_number == room.room_number) {
                roomsArray.set(i, room);
                return true;
            }
        }

        return false;
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    /**
     * Сортировка номеров, в зависимости от текущего поля сортировки.
     * Если текущее поле сортировки не дефолт (не номер комнаты), то
     * необходимо сначала отсортировать данные по номеру комнаты.
     */
    private void sortRooms() {
        if (sortingField != SortingField.ROOM) {
            Collections.sort(mSelectiveLeftRoomsArray, new NumberComparator());
            Collections.sort(mSelectiveRightRoomsArray, new NumberComparator());
            Collections.sort(mFullLeftRoomsArray, new NumberComparator());
            Collections.sort(mFullRightRoomsArray, new NumberComparator());
        }
        Collections.sort(mSelectiveLeftRoomsArray, new RoomComparator());
        Collections.sort(mSelectiveRightRoomsArray, new RoomComparator());
        Collections.sort(mFullLeftRoomsArray, new RoomComparator());
        Collections.sort(mFullRightRoomsArray, new RoomComparator());
        updateList();
    }

    /**
     * Обновление данных в адаптерах
     */
    private void updateList() {
        mLeftListViewAdapter.notifyDataSetChanged();
        mRightListViewAdapter.notifyDataSetChanged();
    }

    /**
     * В зависимости от аргумента, переключает массивы для адаптера и обновляет списки
     *
     * @param show определяет, показывать ли пустые окна
     */
    public void showEmptyRooms(boolean show) {
        if (show) {
            mLeftListViewAdapter = new ListAdapter(mFullLeftRoomsArray, mContext);
            mRightListViewAdapter = new ListAdapter(mFullRightRoomsArray, mContext);
        } else {
            mLeftListViewAdapter = new ListAdapter(mSelectiveLeftRoomsArray, mContext);
            mRightListViewAdapter = new ListAdapter(mSelectiveRightRoomsArray, mContext);
        }

        mLeftListView.setAdapter(mLeftListViewAdapter);
        mRightListView.setAdapter(mRightListViewAdapter);

        mLeftListViewAdapter.notifyDataSetChanged();
        mRightListViewAdapter.notifyDataSetChanged();
    }

    /**
     * Слушатель для кнопок сортировки.
     * При нажатии какой-либо из кнопок:
     * 1) "Отжимаются" остальные кнопки сортировки
     * 2) Устанавливается поле для сортировки
     * 3) Изменяется рисунок кнопки
     * После этого, происходит сортировка и скролл списков на самый верх
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_dnd_flip:
            case R.id.right_dnd_flip:
                mLeftMaidSortButton.setButtonState(false);
                mRightMaidSortButton.setButtonState(false);
                mLeftCleanSortButton.setButtonState(false);
                mRightCleanSortButton.setButtonState(false);
                mLeftWashSortButton.setButtonState(false);
                mRightWashSortButton.setButtonState(false);
                mLeftMinibarSortButton.setButtonState(false);
                mRightMinibarSortButton.setButtonState(false);

                sortingField = (mLeftDNDSortButton.buttonPressed) ? SortingField.ROOM : SortingField.DND;

                mLeftDNDSortButton.changeButtonState();
                mRightDNDSortButton.changeButtonState();

                break;
            case R.id.left_maid_flip:
            case R.id.right_maid_flip:

                mLeftDNDSortButton.setButtonState(false);
                mRightDNDSortButton.setButtonState(false);
                mLeftCleanSortButton.setButtonState(false);
                mRightCleanSortButton.setButtonState(false);
                mLeftWashSortButton.setButtonState(false);
                mRightWashSortButton.setButtonState(false);
                mLeftMinibarSortButton.setButtonState(false);
                mRightMinibarSortButton.setButtonState(false);

                sortingField = (mLeftMaidSortButton.buttonPressed) ? SortingField.ROOM : SortingField.MAID;

                mLeftMaidSortButton.changeButtonState();
                mRightMaidSortButton.changeButtonState();
                break;
            case R.id.left_clean_flip:
            case R.id.right_clean_flip:

                mLeftDNDSortButton.setButtonState(false);
                mRightDNDSortButton.setButtonState(false);
                mLeftMaidSortButton.setButtonState(false);
                mRightMaidSortButton.setButtonState(false);
                mLeftWashSortButton.setButtonState(false);
                mRightWashSortButton.setButtonState(false);
                mLeftMinibarSortButton.setButtonState(false);
                mRightMinibarSortButton.setButtonState(false);

                sortingField = (mLeftCleanSortButton.buttonPressed) ? SortingField.ROOM : SortingField.CLEAN;

                mLeftCleanSortButton.changeButtonState();
                mRightCleanSortButton.changeButtonState();
                break;
            case R.id.left_wash_flip:
            case R.id.right_wash_flip:

                mLeftDNDSortButton.setButtonState(false);
                mRightDNDSortButton.setButtonState(false);
                mLeftMaidSortButton.setButtonState(false);
                mRightMaidSortButton.setButtonState(false);
                mLeftCleanSortButton.setButtonState(false);
                mRightCleanSortButton.setButtonState(false);
                mLeftMinibarSortButton.setButtonState(false);
                mRightMinibarSortButton.setButtonState(false);

                sortingField = (mLeftWashSortButton.buttonPressed) ? SortingField.ROOM : SortingField.WASH;

                mLeftWashSortButton.changeButtonState();
                mRightWashSortButton.changeButtonState();
                break;
            case R.id.left_minibar_flip:
            case R.id.right_minibar_flip:

                mLeftDNDSortButton.setButtonState(false);
                mRightDNDSortButton.setButtonState(false);
                mLeftMaidSortButton.setButtonState(false);
                mRightMaidSortButton.setButtonState(false);
                mLeftCleanSortButton.setButtonState(false);
                mRightCleanSortButton.setButtonState(false);
                mLeftWashSortButton.setButtonState(false);
                mRightWashSortButton.setButtonState(false);

                sortingField = (mLeftMinibarSortButton.buttonPressed) ? SortingField.ROOM : SortingField.MINIBAR;

                mLeftMinibarSortButton.changeButtonState();
                mRightMinibarSortButton.changeButtonState();
                break;
            default:
                break;
        } //switch
        sortRooms();
        mLeftListView.smoothScrollToPosition(0);
        mRightListView.smoothScrollToPosition(0);
    }

    /**
     * Компаратор для сортировки
     */
    public class RoomComparator implements Comparator<Room> {
        @Override
        public int compare(Room o1, Room o2) {
            return o1.compareTo(o2, sortingField);
        }
    }

    /**
     * Компаратор для сортировки по полю "номер комнаты"
     */
    public class NumberComparator implements Comparator<Room> {
        @Override
        public int compare(Room o1, Room o2) {
            return o1.compareTo(o2, sortingField.ROOM);
        }
    }

    public enum SortingField {
        ROOM,
        DND,
        MAID,
        CLEAN,
        WASH,
        MINIBAR
    }
}
