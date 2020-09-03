package com.keesail.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.apache.poi.ss.usermodel.CellType.STRING;

/**
 * 手工导出
 */
public class ExcelUtils {



    /**
     *  生成Excel表格
     * @param sheetName sheet名称
     * @param titleList 表头列表
     * @param dataList 数据列表
     * @return HSSFWorkbook对象
     * */
    public static SXSSFWorkbook createExcel(String sheetName, List<String> titleList,
                                            List dataList) throws IllegalAccessException {

        //创建HSSFWorkbook对象(excel的文档对象)
        SXSSFWorkbook wb = new SXSSFWorkbook();
        //创建sheet对象（excel的表单）
        SXSSFSheet sheet = wb.createSheet(sheetName);
        //在sheet里创建第一行，这里即是表头
        SXSSFRow rowTitle=sheet.createRow(0);

        //写入表头的每一个列
        for (int i = 0; i < titleList.size(); i++) {
            //创建单元格
            rowTitle.createCell(i).setCellValue(titleList.get(i));
            sheet.setColumnWidth(i,20*230);
            //创建Cell样式并设置样式
            CellStyle cStyle=  wb.createCellStyle();
            //水平居中
            cStyle.setAlignment(HorizontalAlignment.CENTER);
        }

        int count = 0;
        //写入每一行的记录
        if (dataList!=null){
            for (int i = 0; i < dataList.size(); i++) {
                count++;
                //创建新的一行，递增
                SXSSFRow rowData = sheet.createRow(i+1);

                //通过反射，获取POJO对象
                Class cl = dataList.get(i).getClass();
                //获取类的所有字段
                Field[] fields = cl.getDeclaredFields();
                for (int j = 0; j < fields.length-1; j++) {
                    //设置字段可见，否则会报错，禁止访问
                    fields[j].setAccessible(true);
                    //创建单元格
                    rowData.createCell(j).setCellValue((String) fields[j].get(dataList.get(i)));
                }
            }
        }
        return wb;
    }

    /**
     * 读入excel文件，解析后返回
     * @param file
     * @throws IOException
     */
    public static List<String[]> readExcel(MultipartFile file) throws IOException {
        //检查文件
        checkFile(file);
        //获得Workbook工作薄对象
        Workbook workbook = getWorkBook(file);
        //创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回
        List<String[]> list = new ArrayList<>();
        if(workbook != null){
            for(int sheetNum = 0;sheetNum < workbook.getNumberOfSheets();sheetNum++){
                //获得当前sheet工作表
                Sheet sheet = workbook.getSheetAt(sheetNum);
                if(sheet == null){
                    continue;
                }
                //获得当前sheet的开始行
                int firstRowNum  = sheet.getFirstRowNum();
                //获得当前sheet的结束行
                int lastRowNum = sheet.getLastRowNum();
                //循环除了第一行的所有行
                for(int rowNum = firstRowNum+1;rowNum <= lastRowNum;rowNum++){
                    //获得当前行
                    Row row = sheet.getRow(rowNum);
                    if(row == null){
                        continue;
                    }
                    //获得当前行的开始列
                    int firstCellNum = row.getFirstCellNum();
                    //获得当前行的列数
                    int lastCellNum = row.getPhysicalNumberOfCells();
                    String[] cells = new String[row.getPhysicalNumberOfCells()];
                    //循环当前行
                    for(int cellNum = firstCellNum; cellNum < lastCellNum;cellNum++){
                        Cell cell = row.getCell(cellNum);
                        cells[cellNum] = getCellValue(cell);
                    }
                    list.add(cells);
                }
            }
            workbook.close();
        }
        return list;
    }

    /**
     * 检查用户上传的文件
     * @param file 文件对象
     * */
    private static void checkFile(MultipartFile file) throws IOException{
        //判断文件是否存在
        if(null == file){
            throw new FileNotFoundException("文件不存在！");
        }
        //获得文件名
        String fileName = file.getOriginalFilename();
        //判断文件是否是excel文件
        if(!fileName.endsWith("xls") && !fileName.endsWith("xlsx")){
            throw new IOException(fileName + "不是excel文件");
        }
    }

    /**
     *  获取Workbook对象
     * @param file 文件对象
     * @return Workbook对象
     * */
    private static Workbook getWorkBook(MultipartFile file) {
        //获得文件名
        String fileName = file.getOriginalFilename();
        //创建Workbook工作薄对象，表示整个excel
        Workbook workbook = null;
        try {
            //获取excel文件的io流
            InputStream is = file.getInputStream();
            //根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
            if(fileName.endsWith("xls")){
                //2003
                workbook = new HSSFWorkbook(is);
            }else if(fileName.endsWith("xlsx")){
                //2007
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {

        }
        return workbook;
    }

    /**
     *  获取单元格的值
     * @param cell 单元格对象
     * @return 值
     * */
    private static String getCellValue(Cell cell){
        String cellValue = "";
        if(cell == null){
            return cellValue;
        }
        //把数字当成String来读，避免出现1读成1.0的情况
        if(cell.getCellType() == NUMERIC){
            cell.setCellType(STRING);
        }
        //判断数据的类型
        switch (cell.getCellType()){
            case NUMERIC: //数字
                cellValue = String.valueOf(cell.getNumericCellValue());
                break;
            case STRING: //字符串
                cellValue = String.valueOf(cell.getStringCellValue());
                break;
            case BOOLEAN: //Boolean
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA: //公式
                cellValue = String.valueOf(cell.getCellFormula());
                break;
            case BLANK: //空值
                cellValue = "未填写";
                break;
            case ERROR: //故障
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }



}
