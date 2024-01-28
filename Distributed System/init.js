db = db.getSiblingDB('ratings_db');
db.ratings_collection.insertMany([
  { product_id: 11, rating: 4 },
  { product_id: 12, rating: 3 }
]);
