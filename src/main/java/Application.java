import javax.persistence.*;
import java.util.List;

public class Application {
    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("tree");
        EntityManager manager = factory.createEntityManager();

        TypedQuery<Tree> treeQuery = manager.createQuery(
                "select t from Tree t order by t.left", Tree.class);
        List<Tree> trees = treeQuery.getResultList();
        for (Tree tree : trees){
            String dash = "";
            for (int i = 0; i < tree.getLevel(); i++){
                dash += "-";
            }
            System.out.println(dash + tree.getName() + " [" + tree.getId() + "]");
        }
    }
}
