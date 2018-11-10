package com;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

import static org.apache.poi.ss.usermodel.CellType.STRING;

public class FieldOfDream {
    int userId;

    public static void main(String[] args) throws IOException {
        System.out.println("Start game");
        int quantity = quantityUsers();
        game(quantity, nameOfUsers(quantity));
    }

    /*FieldOfDream(int userId) throws FileNotFoundException {
        this.userId = userId;
        System.out.println("Start game");
        game(quantity,nameOfUsers(quantity));
    }*/
    private static int quantityUsers() {
        Scanner sc = new Scanner(System.in);
        //вывод картинки
        System.out.println("Введите количество игроков и сыылку на страницы");
        int quantity = sc.nextInt(); //и надо указать id
        if (quantity <= 1) {
            System.out.println("Количество игроков не должно быть меньше 2!");
        }
        return quantity;
    }

    private static String[] nameOfUsers(int quantity) { //private
        // !
        Scanner sc = new Scanner(System.in);
        System.out.println("Введите имена пользователей");
        String[] name = new String[quantity];
        for (int i = 0; i < quantity; i++) {
            name[i] = sc.next();
        }
        return name;
    }

    private static void game(int quantity, String[] name) throws IOException { //private
        //вывод картинки
        System.out.println("Внимание! Игра началась!");
        boolean finish = false;
        termOfWord();
       /* while (!finish) {
            for (int i = 0; i < quantity; i++) {
                System.out.printf("Пользователь %s, ваш ход", name[i]);

            }
        }*/

    }

    private static void termOfWord() throws IOException { // private
        System.out.println("Определение слова");
        XSSFWorkbook exelTermBook = new XSSFWorkbook(new FileInputStream("D:\\Project\\BotForVK\\src\\main\\resources\\terms.xlsx")); // получаю путь к файлу
        XSSFSheet exelTermSheet = exelTermBook.getSheet("terms"); // получаю лист
        Random randomNum = new Random();
        int random = randomNum.nextInt(exelTermSheet.getLastRowNum()+1);
        XSSFRow exelTermRow = exelTermSheet.getRow(random); // получаю строку
        if(exelTermRow.getCell(0).getCellType() == STRING) {
            String term = exelTermRow.getCell(0).getStringCellValue();
        }
        if(exelTermRow.getCell(1).getCellType() == STRING){
            String aboutTerm = exelTermRow.getCell(1).getStringCellValue();
            System.out.println(aboutTerm);
        }
        exelTermBook.close();
    }
}