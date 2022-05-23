package com.pr.productkereview.db;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.pr.productkereview.db.entity.Products;
import com.pr.productkereview.models.AllProducts.ProductModel;

@Database(entities = {Products.class},version = 1)
public abstract class ProductAppDatabase extends RoomDatabase {
    // Linking our DAO to with our database
    public abstract ProductDAO getProductDAO();
}
