package cn.cyandark.app;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author CyanDark
 */
public class ShapeUtil {
    public static final String DEF_GEOM_KEY = "the_geom";
    public static final String DEF_ENCODE = "utf-8";

    /**
     * 图形信息写入shp文件。shape文件中的geometry附带属性类型仅支持String（最大255）、Integer、Double、Boolean、Date(只包含日期，不包含时间)；
     * 附带属性的name仅支持15字符，多余的自动截取。默认编码时utf-8
     *
     * @param shpPath  shape文件路径，包括shp文件名称 如：D:\data\tmp\test.shp
     * @param geomType 图形信息类型 Geometry类型，如Point.class、Polygon.class等
     * @param data     图形信息集合
     */
    public static void createShp(String shpPath, Class<?> geomType, List<Map<String, ?>> data) {
        try {
            createShp(shpPath, DEF_ENCODE, geomType, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 图形信息写入shp文件。shape文件中的geometry附带属性类型仅支持String（最大255）、Integer、Double、Boolean、Date(只包含日期，不包含时间)；
     * 附带属性的name仅支持15字符，多余的自动截取。
     *
     * @param shpPath  shape文件路径，包括shp文件名称 如：D:\data\tmp\test.shp
     * @param encode   shp文件编码
     * @param geomType 图形信息类型 Geometry类型，如Point.class、Polygon.class等
     * @param data     图形信息集合
     */
    public static void createShp(String shpPath, String encode, Class<?> geomType, List<Map<String, ?>> data) {
        try {
            //创建shape文件对象
            File file = new File(shpPath);
            Map<String, Serializable> params = new HashMap<>();
            params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
            params.put(ShapefileDataStoreFactory.DBFCHARSET.key, encode);
            ShapefileDataStore ds = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);
            //定义图形信息和属性信息
            ds.createSchema(builderFeatureType(geomType, CollectionUtils.isEmpty(data) ? null : data.get(0)));
            //设置编码
            Charset charset = Charset.forName(encode);
            ds.setCharset(charset);
            //设置Writer
            FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(ds.getTypeNames()[0], Transaction.AUTO_COMMIT);
            for (Map<String, ?> map : data) {
                //写下一条
                SimpleFeature feature = writer.next();
                for (String key : map.keySet()) {
                    if (DEF_GEOM_KEY.equals(key)) {
                        feature.setAttribute(key, map.get(key));
                    } else {
                        if (map.get(key)!= null && !map.get(key).toString().isEmpty()){
                            feature.setAttribute(key, map.get(key));
                        }
                    }
                }
            }
            writer.write();
            writer.close();
            ds.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取shape文件，默认编码时utf-8
     *
     * @param shpPath shape文件地址，如：D:\data\tmp\test.shp
     * @return shape文件解析后的数据集合
     */
    public static List<Map<String, Object>> readShp(String shpPath) {
        return readShp(shpPath, DEF_ENCODE);
    }

    /**
     * 读取shape文件
     *
     * @param shpPath shape文件地址，如：D:\data\tmp\test.shp
     * @param encode  文件编码
     * @return shape文件解析后的数据集合
     */
    public static List<Map<String, Object>> readShp(String shpPath, String encode) {
        //申明返回结果
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            //加载shape文件
            File file = new File(shpPath);
            //ShapefileDataStore sds = new ShapefileDataStore("");
            Map<String, Object> map = new HashMap<>();
            map.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
            map.put(ShapefileDataStoreFactory.DBFCHARSET.key, Charset.forName(encode));
            //加载数据源
            DataStore ds = DataStoreFinder.getDataStore(map);
            //获取要素源
            FeatureSource<SimpleFeatureType, SimpleFeature> fs = ds.getFeatureSource(ds.getTypeNames()[0]);
            FeatureCollection<SimpleFeatureType, SimpleFeature> collections = fs.getFeatures();
            FeatureIterator<SimpleFeature> features = collections.features();
            //循环获取feature
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                Map<String, Object> featureMap = new HashMap<>();
                for (Property p : feature.getProperties()) {
                    featureMap.put(p.getName().toString(), p.getValue());
                }
                list.add(featureMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 构建Feature模板
     *
     * @param geomType 图形信息类型 Geometry类型，如Point.class、Polygon.class等
     * @param data     图形信息具体的属性
     * @return featureType
     */
    public static SimpleFeatureType builderFeatureType(Class<?> geomType, Map<String, ?> data) {
        //定义图形信息和属性信息
        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setCRS(DefaultGeographicCRS.WGS84);
        ftb.setName("shapefile");
        ftb.add(DEF_GEOM_KEY, geomType);
        if (MapUtils.isNotEmpty(data)) {
            for (String key : data.keySet()) {
                if (Objects.nonNull(data.get(key))) {
                    ftb.add(key, data.get(key).getClass());
                }
            }
        }
        return ftb.buildFeatureType();
    }

    /**
     * 压缩shape文件
     *
     * @param shpPath shape文件路径（包含shape文件名称）
     */
    public static void zipShapeFile(String shpPath) {
        try {
            File shpFile = new File(shpPath);
            String shpRoot = shpFile.getParentFile().getPath();
            String shpName = shpFile.getName().substring(0, shpFile.getName().lastIndexOf("."));
            String zipPath = shpRoot + File.separator + shpName + ".zip";
            File zipFile = new File(zipPath);
            InputStream input;
            ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()));
            // zip的名称为
            zipOut.setComment(shpName);
            String[] shpFiles = new String[]{
                    shpRoot + File.separator + shpName + ".dbf",
                    shpRoot + File.separator + shpName + ".prj",
                    shpRoot + File.separator + shpName + ".shp",
                    shpRoot + File.separator + shpName + ".shx",
                    shpRoot + File.separator + shpName + ".fix"
            };
            for (String s : shpFiles) {
                File file = new File(s);
                input = Files.newInputStream(file.toPath());
                zipOut.putNextEntry(new ZipEntry(file.getName()));
                int temp;
                while ((temp = input.read()) != -1) {
                    zipOut.write(temp);
                }
                input.close();
            }
            deleteResourceShpFiles(shpPath);
            zipOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除服务器shp源文件
     * @param path  shp文件绝对路径
     */
    public static void deleteResourceShpFiles(String path){
        String substring = path.substring(0, path.lastIndexOf('.'));
        String dbf = substring.concat(".dbf");
        String prj = substring.concat(".prj");
        String shp = substring.concat(".shp");
        String shx = substring.concat(".shx");
        String fix = substring.concat(".fix");
        File dbfFile = new File(dbf);
        File prjFile = new File(prj);
        File shpFile = new File(shp);
        File shxFile = new File(shx);
        File fixFile = new File(fix);
        if (dbfFile.exists()){
            dbfFile.delete();
        }
        if (prjFile.exists()) {
            prjFile.delete();
        }
        if (shpFile.exists()) {
            shpFile.delete();
        }
        if (shxFile.exists()) {
            shxFile.delete();
        }
        if (fixFile.exists()) {
            fixFile.delete();
        }
    }
}