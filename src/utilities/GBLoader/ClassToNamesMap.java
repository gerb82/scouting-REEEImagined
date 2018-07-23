package utilities.GBLoader;

import utilities.YouIdiotException;

import java.io.File;

public class ClassToNamesMap {

    private GBDReader reader;

    public ClassToNamesMap(File mapSource){
        reader = new GBDReader(mapSource);
        if(!reader.readHeader().equals("ClassDictionary")){
            throw new YouIdiotException("the data file given as a class dictionary (" + mapSource.getPath() + ") has an invalid header (it is not \"$$ClassDictionary/$$\")");
        }

    }
}
