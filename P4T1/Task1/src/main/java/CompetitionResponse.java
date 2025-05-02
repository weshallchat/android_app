import java.util.List;

class CompetitionsResponse {
    private List<Competition> competitions;

    public List<Competition> getCompetitions() {
        return competitions;
    }
}

class Competition {
    private String name;
    private String code;
    private Area area;

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public Area getArea() {
        return area;
    }
}

class Area {
    private String name;

    public String getName() {
        return name;
    }
}