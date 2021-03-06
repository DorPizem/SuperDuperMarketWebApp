package servlets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.StoreDto;
import dto.StoreItemDto;
import sdm.engine.CutomSDMClasses.EngineManager;
import sdm.engine.CutomSDMClasses.Store;
import sdm.engine.CutomSDMClasses.Utils;
import utils.GsonUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static constants.Constants.*;

/**
 * input:
 *      zoneName: string
 *      storeName: string
 * Output:
 *     StoreDto:
 *            isSucceed: true,false
 *            id: Integer
 *            name: string
 *            owner: string
 *            location: string [x,y]
 *            Set<StoreItemDto> items:
 *                      id: Integer
 *                      name: string
 *                      purchaseCategory: string
 *                      price: Double
 *                      numberOfSells: Double
 *            totalOrders: Integer
 *            totalItemsIncome: Double
 *            ppk: Integer
 *            totalDeliveriesIncome: Double
 *            errorMessage : String
 *
 */

@WebServlet(name = "GetItemsByStoreServlet", urlPatterns = "/getItemsByStore")
public class GetItemsByStoreServlet extends HttpServlet {


    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();
        EngineManager engineManager = UtilsServlet.getEngineManager(getServletContext());
        String rawRequestData = request.getReader().lines().collect(Collectors.joining());
        JsonObject requestData = new JsonParser().parse(rawRequestData).getAsJsonObject();
        String zoneNameParameter = requestData.get(ZONE_NAME).getAsString();
        String storeNameParameter = requestData.get(STORE_NAME).getAsString();
        StoreDto storeDto = new StoreDto();
        try {
            synchronized (getServletContext()) {
                Store store = engineManager.getStoreByStoreNameAndZoneName(storeNameParameter, zoneNameParameter);
                storeDto.setId(store.getID());
                storeDto.setName(store.getName());
                storeDto.setOwner(store.getOwner().getName());
                storeDto.setPpk(store.getPPK());
                storeDto.setTotalDeliveriesIncome(Utils.twoNumbersAfterDot(store.getTotalDeliveriesIncome()));
                storeDto.setTotalOrders(store.getOrders().size());

                Map<Integer, Double> itemIdToPrice = store.getItemIdToPrice();
                Set<StoreItemDto> storeItemsDtos = new HashSet<>();
                for (Integer itemId : itemIdToPrice.keySet()) {
                    StoreItemDto storeItemDto = new StoreItemDto();
                    storeItemDto.setId(itemId);
                    storeItemDto.setPrice(Utils.twoNumbersAfterDot(itemIdToPrice.get(itemId)));
                    storeItemDto.setNumberOfSells(Utils.twoNumbersAfterDot(store.getItemIdToNumberOfSales().get(itemId)));
                    storeItemDto.setName(engineManager.getSuperDuperMarket().getItemByIdAndZone(itemId,
                            engineManager.getSuperDuperMarket().getZoneByName(zoneNameParameter)).getName());
                    storeItemDto.setPurchaseCategory(engineManager.getSuperDuperMarket().getItemByIdAndZone(itemId,
                            engineManager.getSuperDuperMarket().getZoneByName(zoneNameParameter))
                            .getPurchaseCategory().getPurchaseCategoryStr());
                    storeItemsDtos.add(storeItemDto);
                }
                storeDto.setItems(storeItemsDtos);
                storeDto.setSucceed(true);
            }
            out.append(GsonUtils.toJson(storeDto));
        } catch (IllegalArgumentException e){
            storeDto.setSucceed(false);
            storeDto.setErrorMessage(e.getMessage());
            out.append(GsonUtils.toJson(storeDto));
        } catch (Exception e){
            storeDto.setSucceed(false);
            storeDto.setErrorMessage(e.getMessage());
            out.append(GsonUtils.toJson(storeDto));
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
