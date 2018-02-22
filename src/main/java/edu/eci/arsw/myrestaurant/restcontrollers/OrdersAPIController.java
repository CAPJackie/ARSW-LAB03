/*
 * Copyright (C) 2016 Pivotal Software, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.eci.arsw.myrestaurant.restcontrollers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.arsw.myrestaurant.model.Order;
import edu.eci.arsw.myrestaurant.model.ProductType;
import edu.eci.arsw.myrestaurant.model.RestaurantProduct;
import edu.eci.arsw.myrestaurant.services.RestaurantOrderServices;
import edu.eci.arsw.myrestaurant.services.RestaurantOrderServicesStub;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 *
 * @author hcadavid
 */
import edu.eci.arsw.myrestaurant.services.OrderServicesException;
import java.io.IOException;
import java.util.ArrayList;
@Service
@RestController
@RequestMapping(value = "/orders")
public class OrdersAPIController {
    @Autowired
    RestaurantOrderServices ros;
    
    Gson g = new Gson();
    
    
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getOrders(){
        Map<Integer, Order> map = new HashMap<>();
        Set<Integer> keys = ros.getTablesWithOrders();
        keys.forEach((i) -> {
            try {
                map.put(i, ros.getTableOrder(i));
            } catch (OrderServicesException ex) {
                Logger.getLogger(OrdersAPIController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });               
        //String mapToJson = g.toJson(map);
        return new ResponseEntity<>(map,HttpStatus.OK);
    }
    
    @RequestMapping(method = RequestMethod.GET, path = "/{idTable}")
    public ResponseEntity<?> getOrder(@PathVariable int idTable){
        try{
            Map<Integer, Order> map = new HashMap<>();
            map.put(idTable, ros.getTableOrder(idTable));             
            //String mapToJson = g.toJson(map);
            return new ResponseEntity<>(map,HttpStatus.OK);
        } catch(OrderServicesException e){
            Logger.getLogger(OrdersAPIController.class.getName()).log(Level.SEVERE, null, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @RequestMapping(method = RequestMethod.POST)	
    public ResponseEntity<?> addOrder(@RequestBody Map<Integer, Order> order){
            try {
                //Type listType = new TypeToken<Map<String, Order>>(){}.getType();
                //Map<String, Order> map = g.fromJson(order, listType);
                //Set<String> keys = map.keySet();
                Set<Integer> keys = order.keySet();
                for(Integer s: keys){
                    ros.addNewOrderToTable(order.get(s));
                }
                return new ResponseEntity<>(HttpStatus.OK);          
            } catch(OrderServicesException e){
                Logger.getLogger(OrdersAPIController.class.getName()).log(Level.SEVERE, null, e);
                return new ResponseEntity<>(e.getMessage(),HttpStatus.METHOD_NOT_ALLOWED); 
            }

    }
    
    @RequestMapping(method = RequestMethod.GET, path = "/{idTable}/total")
    public ResponseEntity<?> getTotalTableBill(@PathVariable int idTable){
        try {
            return new ResponseEntity<>(ros.calculateTableBill(idTable),HttpStatus.OK);
        } catch (OrderServicesException ex) {
            Logger.getLogger(OrdersAPIController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    
    @RequestMapping(method = RequestMethod.PUT, path = "{idTable}")
    public ResponseEntity<?> updateOrder(@PathVariable int idTable, @RequestBody Map<String, Integer> plato){
        //Type tipo = new TypeToken<Map<String, String>>(){}.getType();
        //Map<String, String> map = g.fromJson(plato, tipo);
        for(String key: plato.keySet()){
            try {
                ros.getTableOrder(idTable).addDish(key, plato.get(key));               
            } catch (OrderServicesException ex) {
                Logger.getLogger(OrdersAPIController.class.getName()).log(Level.SEVERE, null, ex);
                return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);               
        
    }
    
    
    @RequestMapping(method = RequestMethod.DELETE, path = "{idTable}")
    public ResponseEntity<?> deleteOrder(@PathVariable int idTable){
        try {
            ros.releaseTable(idTable);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (OrderServicesException ex) {
            Logger.getLogger(OrdersAPIController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
            
        }
    }
}

