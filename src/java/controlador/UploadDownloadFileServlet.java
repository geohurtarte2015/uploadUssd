
package controlador;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.usermodel.HSSFSheet;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;





@MultipartConfig
public class UploadDownloadFileServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
     
    // location to store file uploaded
    private static final String UPLOAD_DIRECTORY = "upload";
 
    // upload settings
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB

 
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("multipart/form-data");

    }

  
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

   
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
         if (!ServletFileUpload.isMultipartContent(request)) {
            // if not, we stop here
            PrintWriter writer = response.getWriter();
            writer.println("Error: Form must has enctype=multipart/form-data.");
            writer.flush();
            return;
        }
 
        // configures upload settings
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // sets memory threshold - beyond which files are stored in disk
        factory.setSizeThreshold(MEMORY_THRESHOLD);
        // sets temporary location to store files
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
 
        ServletFileUpload upload = new ServletFileUpload(factory);
         
        // sets maximum size of upload file
        upload.setFileSizeMax(MAX_FILE_SIZE);
         
        // sets maximum size of request (include file + form data)
        upload.setSizeMax(MAX_REQUEST_SIZE);
 
        // constructs the directory path to store upload file
        // this path is relative to application's directory
        String uploadPath = getServletContext().getRealPath("")
                + File.separator + UPLOAD_DIRECTORY;
         
        String filePath = "";
        // creates the directory if it does not exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
 
        try {
            // parses the request's content to extract file data
            @SuppressWarnings("unchecked")
            List<FileItem> formItems = upload.parseRequest(request);
 
            if (formItems != null && formItems.size() > 0) {
                // iterates over form's fields
                for (FileItem item : formItems) {
                    // processes only fields that are not form fields
                    if (!item.isFormField()) {
                        String fileName = new File(item.getName()).getName();
                        filePath = uploadPath + File.separator + fileName;
                        File storeFile = new File(filePath);
           
                        // saves the file on disk
                        item.write(storeFile);
                        
                    
                        request.setAttribute("message",
                            "Upload has been done successfully!");
                    }
                }
            }
        } catch (Exception ex) {
            request.setAttribute("message",
                    "There was an error: " + ex.getMessage());
        }
        
        
        
        readSheet(filePath);
        System.out.println("leido");
        
        
                       
                   
        // redirects client to message page
        getServletContext().getRequestDispatcher("/index.jsp").forward(
                request, response);
    }
            
    
   
     
     public ArrayList<String[]> readSheet(String path)
        {
        ArrayList<String[]> list = new ArrayList<String[]>();
        try {

            DataFormatter fmt = new DataFormatter();
            FileInputStream file = new FileInputStream(path);

            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);

            //Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                //For each row, iterate through all the columns
                Iterator<Cell> cellIterator = row.cellIterator();
                String[] values = new String[9];

                for (int x = 0; x < 9; x++) {
                    values[x] = "";
                }

                int i = 0;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    //Check the cell type after eveluating formulae
                    //If it is formula cell, it will be evaluated otherwise no change will happen
                    switch (evaluator.evaluateInCell(cell).getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            System.out.print(fmt.formatCellValue(cell) + " | ");
                            values[i] = fmt.formatCellValue(cell);

                            break;
                        case Cell.CELL_TYPE_STRING:
                            System.out.print(cell.getStringCellValue() + " | ");
                            values[i] = fmt.formatCellValue(cell);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            //Not again
                            break;
                    }
                    i++;
                }
                System.out.println("");
                list.add(values);
            }
            file.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
}
     
//    public int insertDataCdr(ArrayList<String[]> dataCdr) throws IOException{
//        
//    String classfor="oracle.jdbc.OracleDriver";
//    Connection con=null;
//    PreparedStatement pr=null;
//    Statement ps=null;
//    ResultSet rs=null;    
//    
//    String host = "172.24.0.7";
//    String userdb = "PREPAGO";
//    String passdb = "PREPAGO";
//    String port = "1525";
//    String valconnect = "BEMOBILE";
//    String connect = "SID";
//    String usuario=userdb;
//    String clave=passdb;
//    String url="jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST="+host+")(PORT="+port+"))(CONNECT_DATA=("+connect+"="+valconnect+")))";
//        String sql=null;
//        int id=0;
//        try
//        {   
//            Class.forName(classfor);
//            con=DriverManager.getConnection(url, usuario,clave);
//            System.out.println("Connected database successfully...");
//            
//            ps=con.createStatement();
//            
//            for(CdrSms cdr : dataCdr){            
//          
//               sql="INSERT INTO TB_FILE_CDR_MERKA (ID,SEQUENCE_NUMBER,NAME_FILE, CDR_DATE_CREATE, CDR_DATE_PROCESSED,DESTINATION_NUMBER,SUBSCRIBER_ID,STATUS_CDR)" +
//    "values("+"SEQ_FILE_CDR_MERKA.nextval"+
//                             
//                       ",'"+
//                  cdr.getSequenceNumber() +
//                                   "','" + 
//                       cdr.getFileName() +
//                                   "','" + 
//                  cdr.getCdrDate() +
//                                   "','" + 
//                cdr.getDateReadCdr()+
//                                   "','" + 
//               cdr.getDestinationNumber()+
//                                   "','" + 
//                    cdr.getSubscriberId()+
//                                   "','" + 
//                      cdr.getStatus() +
//                                     "')";
//                    //System.out.println(sql);
//                ps.executeUpdate(sql);
//            }
//            id=1;
//            
//        }
//        catch (Exception exception){
//            System.out.println("Exception : " + exception.getMessage() + "  "+"error en base de datos");      
//        }
//        finally
//	{
//		if(con != null)
//		{
//			try
//			{
//                                pr.close();
//				con.close();
//			}
//			catch (Exception ignored)
//			{
//				// ignore
//			}
//				
//		}
//	}
//             
//          
//      return id;
//      }
//   


 

}
