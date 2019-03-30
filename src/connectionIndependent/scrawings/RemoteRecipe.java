package connectionIndependent.scrawings;

public interface RemoteRecipe {

    <T extends RemoteRecipe> T replicate();
}
