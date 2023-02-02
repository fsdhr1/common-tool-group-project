package com.gykj.commontool.networktest.beans;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SourceInfoBean implements Serializable {
    private List<VectorLayers> vector_layers;

    public static class VectorLayers implements Serializable {
        private String description;
        private String id;
        private Map<String, String> fieldalias;
        private Map<String, String> fields;

       public Map<String, String> getFieldalias() {
          return fieldalias;
       }

       public void setFieldalias(Map<String, String> fieldalias) {
          this.fieldalias = fieldalias;
       }

       public Map<String, String> getFields() {
          return fields;
       }

       public void setFields(Map<String, String> fields) {
          this.fields = fields;
       }

       public void setDescription(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }

    }

    public List<VectorLayers> getVector_layers() {
        return vector_layers;
    }

    public void setVector_layers(List<VectorLayers> vector_layers) {
        this.vector_layers = vector_layers;
    }
}