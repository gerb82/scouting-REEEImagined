package connectionIndependent.Scrawings;

public interface RemoteRecipe {

    <T extends RemoteRecipe> T replicate();
}
