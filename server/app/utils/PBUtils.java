package utils;

import com.conveyal.transitwand.TransitWandProtos;
import com.google.protobuf.InvalidProtocolBufferException;
import play.Logger;

import java.io.*;
/**
 * Clase para manejar fichero ProtocolBuffer creado al subir las rutas desde el teléfono
 */
public class PBUtils {

    /**
     * Parsea el fichero PB regresa la entidad
     * @param filePath ruta donde está el fichero ProtocolBuffer
     * @return 
     */
    public static TransitWandProtos.Upload parsePB(String filePath) {
        if(filePath == null) {
            Logger.error("Debe proporcionar una ruta de fichero válida.");
            return null;
        }
        try {
            File pbFile = new File(filePath);
            byte[] dataFrame = new byte[(int) pbFile.length()];
            DataInputStream dataInputStream = null;
            dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(pbFile)));
            dataInputStream.read(dataFrame);
            TransitWandProtos.Upload upload = TransitWandProtos.Upload.parseFrom(dataFrame);
            return upload;
        } catch (FileNotFoundException e) {
            Logger.error("No existe el fichero");
        } catch (InvalidProtocolBufferException e) {
            Logger.error("InvalidProtocolBufferException:" + e.getMessage());
            
        } catch (IOException e) {
            Logger.error("IO Exception:" + e.getMessage());
        }
        return null;
    }

}
