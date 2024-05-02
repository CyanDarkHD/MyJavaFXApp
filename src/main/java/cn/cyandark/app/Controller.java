package cn.cyandark.app;

import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {
    @FXML
    private Label welcomeText;
    @FXML
    private Button excelChooser;
    @FXML
    private Button outputChooser;
    @FXML
    private RadioButton point;
    @FXML
    private RadioButton line;
    @FXML
    private ToggleGroup slType;
    @FXML
    private Label tips;

    @FXML
    protected void onFileBtnSelect() throws IOException {
        tips.setVisible(true);
        FileChooser chooser = new FileChooser();
        chooser.setTitle("请选择文件");
        // 设置文件选择器的初始目录，这里设置为用户的主目录
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        // 设置文件选择器的过滤器
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel表格", "*.xlsx", "*.xls")
        );
        // 显示文件选择器，并获取用户选择的文件（如果有）
        File file = chooser.showOpenDialog(excelChooser.getScene().getWindow());
        if (file == null) {
            tips.setVisible(false);
            return;
        }
        // 处理用户选择的文件
        XSSFWorkbook sheets;
        XSSFSheet sheet;
        try {
            sheets = new XSSFWorkbook(file);
            sheet = sheets.getSheet("管线(map_supply_pipe)");
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        }
        XSSFRow properties = sheet.getRow(2);
        List<Map<String, ?>> data = new ArrayList<>();
        for (Row cells : sheet) {
            // 跳过表头循环
            if (cells.getRowNum() < 6) {
                continue;
            }
            // 处理图形信息
            GeometryJSON geometryJson = new GeometryJSON();
            Geometry read;
            String startX = cells.getCell(3).toString().trim();
            String startY = cells.getCell(4).toString().trim();
            String endX = cells.getCell(5).toString().trim();
            String endY = cells.getCell(6).toString().trim();
            if (startX.isEmpty()) {
                tips.setVisible(false);
                break;
            }
            String position = "[[".concat(startY).concat(",").concat(startX).concat("],[").concat(endY).concat(",").concat(endX).concat("]]");
            ArrayList<Object> coordinates = new ArrayList<>(JSONUtil.parseArray(JSONUtil.toJsonStr(position)));
            Map<String, Object> geoJson = new HashMap<>(2);
            // 线段类型GeoJSON
            geoJson.put("type", "LineString");
            geoJson.put("coordinates", coordinates);
            Gson gson = new Gson();
            read = geometryJson.read(gson.toJson(geoJson));
            Map<String, Object> map = new HashMap<>();
            // 存入图形信息
            map.put(ShapeUtil.DEF_GEOM_KEY, read);
            //    DBF文件的属性
            for (Cell cell : cells) {
                String property = properties.getCell(cell.getColumnIndex()).toString();
                if (property.length() > 9) {
                    property = property.substring(0, 9);
                }
                map.put(property, cell);
            }
            // 写入一条shp信息
            data.add(map);
        }
        tips.setText("解析完成，请选择保存目录");
        DirectoryChooser outputFileChooser = new DirectoryChooser();
        outputFileChooser.setTitle("请选择矢量文件保存目录");
        // 设置文件选择器的初始目录，这里设置为用户的主目录
        outputFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        // 显示文件选择器，并获取用户选择的文件（如果有）
        File outputPath = outputFileChooser.showDialog(outputChooser.getScene().getWindow());
        if (outputPath == null) {
            return;
        }
        //  输出路径
        ShapeUtil.createShp(outputPath + "\\output.shp", "GBK", LineString.class, data);
        ShapeUtil.zipShapeFile(outputPath + "\\output.shp");
        tips.setText("转换成功！");
    }

    @FXML
    protected void onPointBtnSelect() throws IOException {
        tips.setVisible(true);
        FileChooser chooser = new FileChooser();
        chooser.setTitle("请选择文件");
        // 设置文件选择器的初始目录，这里设置为用户的主目录
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        // 设置文件选择器的过滤器
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel表格", "*.xlsx", "*.xls")
        );
        // 显示文件选择器，并获取用户选择的文件（如果有）
        File file = chooser.showOpenDialog(excelChooser.getScene().getWindow());
        if (file == null) {
            tips.setVisible(false);
            return;
        }
        // 处理用户选择的文件
        XSSFWorkbook sheets;
        XSSFSheet sheet;
        try {
            sheets = new XSSFWorkbook(file);
            sheet = sheets.getSheet("Sheet1");
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        }
        List<Map<String, ?>> data = new ArrayList<>();
        for (Row cells : sheet) {
            // 跳过表头循环
            if (cells.getRowNum() < 3) {
                continue;
            }
            // 处理图形信息
            GeometryJSON geometryJson = new GeometryJSON();
            Geometry read;
            String x = cells.getCell(2).toString().trim();
            String y = cells.getCell(3).toString().trim();
            if (x.isEmpty()) {
                break;
            }
            String position = "[".concat(x).concat(",").concat(y).concat("]");
            ArrayList<Object> coordinates = new ArrayList<>(JSONUtil.parseArray(JSONUtil.toJsonStr(position)));
            Map<String, Object> geoJson = new HashMap<>(2);
            // 点类型GeoJSON
            geoJson.put("type", "Point");
            geoJson.put("coordinates", coordinates);
            Gson gson = new Gson();
            read = geometryJson.read(gson.toJson(geoJson));
            Map<String, Object> map = new HashMap<>();
            // 存入图形信息
            map.put(ShapeUtil.DEF_GEOM_KEY, read);
            //    DBF文件的属性
            map.put("id", cells.getCell(0));
            map.put("desc", cells.getCell(1));
            map.put("Y", cells.getCell(2));
            map.put("X", cells.getCell(3));
            map.put("altitude", cells.getCell(4));
            map.put("depth", cells.getCell(5));
            map.put("btm_depth", cells.getCell(6));
            map.put("material", cells.getCell(7));
            map.put("remarks", cells.getCell(8));
            // 写入一条shp信息
            data.add(map);
        }
        tips.setText("解析完成，请选择保存目录");
        DirectoryChooser outputFileChooser = new DirectoryChooser();
        outputFileChooser.setTitle("请选择矢量文件保存目录");
        // 设置文件选择器的初始目录，这里设置为用户的主目录
        outputFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        // 显示文件选择器，并获取用户选择的文件（如果有）
        File outputPath = outputFileChooser.showDialog(outputChooser.getScene().getWindow());
        if (outputPath == null) {
            return;
        }
        //  输出路径
        ShapeUtil.createShp(outputPath + "\\output.shp", "GBK", Point.class, data);
        ShapeUtil.zipShapeFile(outputPath + "\\output.shp");
        tips.setText("转换成功！");
    }
}