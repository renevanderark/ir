package nl.kb.dare.model.reporting;

public class ExcelReportRow {
    private final Integer statusCode;
    private final String tsCreate;
    private final String message;
    private final String url;
    private final String oaiId;
    private final String tsProcessed;
    private final Integer state;
    private final String kbObjId;
    private final String oaiDatestamp;

    public ExcelReportRow(ExcelReportRowBuilder builder) {
        this.statusCode = builder.statusCode;
        this.tsCreate = builder.tsCreate;
        this.message = builder.message;
        this.url = builder.url;
        this.oaiId = builder.oaiId;
        this.tsProcessed = builder.tsProcessed;
        this.state = builder.state;
        this.kbObjId = builder.kbObjId;
        this.oaiDatestamp = builder.oaiDatestamp;
    }


    public Integer getStatusCode() {
        return statusCode;
    }

    public String getTsCreate() {
        return tsCreate;
    }

    public String getMessage() {
        return message;
    }

    public String getUrl() {
        return url;
    }

    public String getOaiId() {
        return oaiId;
    }

    public String getTsProcessed() {
        return tsProcessed;
    }

    public Integer getState() {
        return state;
    }

    public String getKbObjId() {
        return kbObjId;
    }

    public String getOaiDatestamp() {
        return oaiDatestamp;
    }

    public static class ExcelReportRowBuilder {
        private Integer statusCode;
        private String tsCreate;
        private String message;
        private String url;
        private String oaiId;
        private String tsProcessed;
        private Integer state;
        private String kbObjId;
        private String oaiDatestamp;

        public ExcelReportRowBuilder setStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public ExcelReportRowBuilder setTsCreate(String tsCreate) {
            this.tsCreate = tsCreate == null ? "" : tsCreate;
            return this;
        }

        public ExcelReportRowBuilder setMessage(String message) {
            this.message = message == null ? "" : message;
            return this;
        }

        public ExcelReportRowBuilder setUrl(String url) {
            this.url = url == null ? "" : url;
            return this;
        }

        public ExcelReportRowBuilder setOaiId(String oaiId) {
            this.oaiId = oaiId;
            return this;
        }

        public ExcelReportRowBuilder setTsProcessed(String tsProcessed) {
            this.tsProcessed = tsProcessed == null ? "" : tsProcessed;
            return this;
        }

        public ExcelReportRowBuilder setState(Integer state) {
            this.state = state;
            return this;
        }

        public ExcelReportRowBuilder setKbObjId(String kbObjId) {
            this.kbObjId = kbObjId == null ? "" : kbObjId;
            return this;
        }

        public ExcelReportRowBuilder setOaiDatestamp(String oaiDatestamp) {
            this.oaiDatestamp = oaiDatestamp;
            return this;
        }

        public ExcelReportRow createExcelReportRow() {
            return new ExcelReportRow(this);
        }
    }
}
