import javax.persistence.*;
import java.util.List;
import java.util.Scanner;

public class TreeAdd {
    public static void main(String[] args) {

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("tree");
        EntityManager manager = factory.createEntityManager();


// Updates;
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n" + "Введите ID категории:");
        long idInput = Long.parseLong(scanner.nextLine());

        TypedQuery<Long> treeCountQuery = manager.createQuery(
                "select count (c.id) from Tree c where c.id =?1", Long.class);
        treeCountQuery.setParameter(1, idInput);
        long idCount = treeCountQuery.getSingleResult();
        if (idInput == 0) {
            TypedQuery<Integer> queryLvl = manager.createQuery(
                    "select max(t.right) from Tree t", Integer.class);
            int maxRight = queryLvl.getSingleResult();
            System.out.println("Max right: " + maxRight);
            String newNameLvl = scanner.nextLine();

            try {
                manager.getTransaction().begin();

                Tree newLvlTree = new Tree();
                newLvlTree.setName(newNameLvl);
                newLvlTree.setLevel(0);
                newLvlTree.setLeft(maxRight + 1);
                newLvlTree.setRight(maxRight + 2);
                manager.persist(newLvlTree);

                manager.getTransaction().commit();
            } catch (Exception e) {
                manager.getTransaction().rollback();
                e.printStackTrace();
            }

        } else {
            while (idCount == 0) {
                System.out.println("\n" + "Данного ID нет");
                System.out.println("\n" + "Введите ID категории:");

                idInput = Long.parseLong(scanner.nextLine());
                treeCountQuery = manager.createQuery(
                        "select count (c.id) from Tree c where c.id =?1", Long.class);
                treeCountQuery.setParameter(1, idInput);
                idCount = treeCountQuery.getSingleResult();
            }

            System.out.println("Введите название:");
            String nameOfCat = scanner.nextLine();

            try {
                manager.getTransaction().begin();
                Tree tree = manager.find(Tree.class, idInput);
                Query leftQuery = manager.createQuery(
                        "UPDATE Tree t SET t.left = t.left + 2  where t.left > ?1");
                leftQuery.setParameter(1, tree.getRight());
                leftQuery.executeUpdate();

                Query rightQuery = manager.createQuery(
                        "UPDATE Tree t SET t.right = t.right + 2 where t.right >= ?1");
                rightQuery.setParameter(1, tree.getRight());
                rightQuery.executeUpdate();


                Tree newTree = new Tree();

                newTree.setLevel(tree.getLevel() + 1);
                newTree.setName(nameOfCat);
                newTree.setLeft(tree.getRight());
                newTree.setRight(tree.getRight() + 1);
                manager.persist(newTree);


                manager.getTransaction().commit();
            } catch (Exception e) {
                manager.getTransaction().rollback();
                e.printStackTrace();
            }
        }
    }
}

//
//        # Введите ID категории: 2
//        # Введите название: МЦСТ
//
//        # Правый ключ должен быть увеличен на 2 у всех элементов, правый ключ которых
//        # больше или равен правому ключу родительского элемента.
//        # Левый ключ должен быть увеличен на 2 у всех элементов, левый ключ которых
//        # больше правого ключа родительского элемента.

// добавить новый элемент на 0 уровне

// удаление элементов


//    TypedQuery<Tree> treeQuery = manager.createQuery(
//            "select t from Tree t order by t.left", Tree.class);
//    List<Tree> trees = treeQuery.getResultList();
//        for (Tree tree : trees){
//                String dash = "";
//                for (int i = 0; i < tree.getLevel(); i++){
//        dash += "-";
//        }
//        System.out.println(dash + tree.getName() + " [" + tree.getId() + "]");
//        }
