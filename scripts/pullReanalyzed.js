db.datasets.similars.update({"database":/biomodels/i},{"$pull":{"similars":{"relationType":"Reanalyzed by"}}},false,true)