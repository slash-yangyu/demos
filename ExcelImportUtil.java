package com.keesail.service.util;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.apache.poi.ss.usermodel.CellType.*;

/**
 * Excel导入工具类
 */
public class ExcelImportUtil {

    /**
     * 根据文件选择excel版本
     *
     * @return
     * @throws Exception
     */
    public static Workbook chooseWorkbook(MultipartFile file) throws Exception {

        Workbook workbook = null;

        //把MultipartFile转化为File
        //1.1创建临时文件dfile
//        File fo = File.createTempFile("prefix", "_" + file.getOriginalFilename());
        //1.2将file文件内容转储到dfile临时文件中
//        file.transferTo(fo);

        String fileName = file.getOriginalFilename();
        InputStream is = file.getInputStream();
        if(fileName.endsWith("xls")){
            //2003
            workbook = new HSSFWorkbook(is);
        }else if(fileName.endsWith("xlsx")){
            //2007
            workbook = new XSSFWorkbook(is);
        }
        return workbook;
    }


    /**
     * 公共的导入excel方法
     * @param file
     * @param obj
     * @param strings
     * @return
     * @throws IOException
     */
    public static List<Object> importBaseExcel(MultipartFile file,  Class<?> obj, String[] strings) throws IOException {

        Workbook workbook = null;

        try {
            //读取文件内容
            workbook = chooseWorkbook(file);
//            workbook = getWorkBook(file);
            //获取工作表
//            workbook = new HSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            //获取sheet中第一行行号
            int firstRowNum = sheet.getFirstRowNum();
            //获取sheet中最后一行行号
            int lastRowNum = sheet.getLastRowNum();

            //获取该实体所有定义的属性 返回Field数组
            java.lang.reflect.Field[] entityName = obj.getDeclaredFields();


            //验证Excel表头是否与模板一致
            List<Object> list = new ArrayList<Object>();
            String[] str = new String[strings.length];
            for (int i = 0; i < strings.length; i++) {
                Cell pname = sheet.getRow(0).getCell(i);
                String trim = getVal(pname).trim();
                str[i] = trim;
            }
            if (!Arrays.equals(str, strings)) {
                list.add("containFalse");
                return list;
            }
            //循环插入数据classname
            for (int i = firstRowNum + 1; i <= lastRowNum; i++) {

                Row row = sheet.getRow(i);
                if(row == null){
                    return list;
                }

                //可以根据该类名生成Java对象
                Object pojo = obj.newInstance();

                //定义一个空值数量记录的变量
                int count = 0;

                //除自增编号外，实体字段匹配sheet列
                for (int j = 0; j < strings.length; j++) {

                    //获取属性的名字,将属性的首字符大写，方便构造set方法
                    String name = "set" + entityName[j].getName().substring(0, 1).toUpperCase().concat(entityName[j].getName().substring(1)) + "";
                    //获取属性的类型
                    String type = entityName[j].getGenericType().toString();

                    //getMethod只能调用public声明的方法，而getDeclaredMethod基本可以调用任何类型声明的方法
                    Method m = obj.getDeclaredMethod(name, entityName[j].getType());

                    Cell pname = row.getCell(j);

                    //根据属性类型装入值
                    switch (type) {
                        case "char":
                        case "java.lang.Character":
                        case "class java.lang.String":
                            String strVal = getVal(pname).trim();
                            if (StringUtils.isEmpty(strVal)) {
                                count++;
                            }
                            m.invoke(pojo, strVal);
                            break;
                        case "int":
                        case "class java.lang.Integer":
                            String intVal = getVal(pname).trim();
                            if (StringUtils.isEmpty(intVal)) {
                                count++;
                                break;
                            } else {
                                m.invoke(pojo, Integer.valueOf(intVal));
                            }
                            break;
                        case "class java.util.Date":
                            String dateVal = getVal(pname).trim();
                            if (StringUtils.isEmpty(dateVal)) {
                                count++;
                                break;
                            } else {
                                String regex = "^\\d{4}-\\d{2}-\\d{2}$";
                                if (!dateVal.matches(regex)) {
                                    list.add("DateError");
                                    return list;
                                }
                                Date parse = new SimpleDateFormat("yyyy-MM-dd").parse(dateVal);
                                m.invoke(pojo, parse);
                            }
                            break;
                        case "float":
                        case "double":
                        case "java.lang.Double":
                        case "java.lang.Float":
                        case "java.lang.Long":
                        case "java.lang.Short":
                        case "class java.math.BigDecimal":
                            String bdVal = getVal(pname).trim();
                            if (StringUtils.isEmpty(bdVal)) {
                                count++;
                                break;
                            } else {
                                m.invoke(pojo, BigDecimal.valueOf(Double.valueOf(bdVal)));
                            }
                            break;
                         case "class java.lang.Long":
                             String longVal = getVal(pname).trim();
                             if (StringUtils.isEmpty(longVal)) {
                                 count++;
                                 break;
                             } else {
                                 m.invoke(pojo, Long.valueOf(longVal));
                             }
                             break;
                        default:
                            break;
                    }
                }
                if (count == entityName.length - 1) {
                    return list;
                }
                list.add(pojo);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workbook.close();
        }
        return null;
    }

    /**
     * 处理类型
     *
     * @param cell
     * @return
     */
    public static String getVal(Cell cell) {
        if (null != cell) {
            if(cell.getCellTypeEnum() == NUMERIC){
                if (!HSSFDateUtil.isCellDateFormatted(cell)) {
                    cell.setCellType(STRING);
                }
            }

            switch (cell.getCellTypeEnum()) {
                case NUMERIC: // 数字
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        Date date = cell.getDateCellValue();
                        String month = "" + (date.getMonth() + 1);
                        String day = "" + date.getDate();
                        if (date.getMonth() + 1 < 10) {
                            month = "0" + month;
                        }
                        if (date.getDate() < 10) {
                            day = "0" + day;
                        }
                        return (date.getYear() + 1900) + "-" + month + "-" + day;
                    } else if (String.valueOf(cell.getNumericCellValue()).indexOf("E") == -1) {
                        //表格数字小于十位时处理
                        String val = cell.getNumericCellValue() + "";
                        int index = val.indexOf(".");

                        if (Double.valueOf(val.substring(index + 1)) == 0) {
                            DecimalFormat df = new DecimalFormat("0");//处理科学计数法
                            return df.format(cell.getNumericCellValue());
                        }
                        DecimalFormat df = new DecimalFormat("#.00");//处理科学计数法
                        String format = df.format(cell.getNumericCellValue());
                        if(format.startsWith(".")){
                            format="0"+format;
                        }
                        return format;
//                        return cell.getNumericCellValue() + "";//double
                    } else {
                        //表格数字大于十位时处理
                        return new DecimalFormat("#").format(cell.getNumericCellValue());
                    }
                case STRING: // 字符串
                    return cell.getStringCellValue();
                case BOOLEAN: // Boolean
                    return cell.getBooleanCellValue() + "";
                case FORMULA: // 公式
                    try {
                        if (HSSFDateUtil.isCellDateFormatted(cell)) {
                            Date date = cell.getDateCellValue();
                            return (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate();
                        } else {
                            return String.valueOf((int) cell.getNumericCellValue());
                        }
                    } catch (IllegalStateException e) {
                        return String.valueOf(cell.getRichStringCellValue());
                    }
                case BLANK: // 空值
                    return "";
                case ERROR: // 故障
                    return "";
                default:
                    return "unknown type";
            }
        } else {
            return "";
        }
    }


}
