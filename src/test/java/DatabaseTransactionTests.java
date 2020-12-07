import com.neo4j.causalclustering.common.Cluster;
import com.neo4j.causalclustering.core.CoreClusterMember;
import com.neo4j.configuration.CausalClusteringSettings;
import com.neo4j.test.causalclustering.ClusterConfig;
import com.neo4j.test.causalclustering.ClusterExtension;
import com.neo4j.test.causalclustering.ClusterFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.TransactionEventListenerAdapter;
import org.neo4j.test.extension.Inject;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;
import static org.neo4j.graphdb.Label.label;

@TestInstance( TestInstance.Lifecycle.PER_METHOD )
@ClusterExtension
public class DatabaseTransactionTests {
    @Inject
    private ClusterFactory clusterFactory;

    private Cluster cluster;

    private final ClusterConfig clusterConfig = ClusterConfig
            .clusterConfig()
            .withNumberOfCoreMembers( 3 )
            .withSharedCoreParam( CausalClusteringSettings.minimum_core_cluster_size_at_formation, "3" )
            .withNumberOfReadReplicas( 0 );

    @BeforeEach
    void setup() throws Exception
    {
        cluster = clusterFactory.createCluster( clusterConfig );
        cluster.start();
    }

    @Test
    void createNodeTest() throws Exception
    {
        // Do work here
        FederosTransactionEventListenerAdapter listener = new FederosTransactionEventListenerAdapter();
        for (CoreClusterMember coreMember : cluster.coreMembers()) {
            coreMember.managementService().registerTransactionEventListener(DEFAULT_DATABASE_NAME, listener);
        }

        cluster.coreTx( ( db, tx ) ->
        {
            Node node = tx.createNode( label( "boo" ) );
            node.setProperty( "foobar", "baz_bat" );
            tx.commit();
        } );
    }

    @Test
    void createNodesTest() throws Exception
    {
        // Do work here
        FederosTransactionEventListenerAdapter listener = new FederosTransactionEventListenerAdapter();
        for (CoreClusterMember coreMember : cluster.coreMembers()) {
            coreMember.managementService().registerTransactionEventListener(DEFAULT_DATABASE_NAME, listener);
        }

        cluster.coreTx( ( db, tx ) ->
        {
            tx.execute("CREATE (t:Test {uuid:'123XYZ'})");
            tx.execute("CREATE (t2:Test {uuid:'XZY123'})");
            tx.commit();
        } );
    }
    @Test
    void mergeNewNodeTest() throws Exception
    {
        // Do work here
        FederosTransactionEventListenerAdapter listener = new FederosTransactionEventListenerAdapter();
        for (CoreClusterMember coreMember : cluster.coreMembers()) {
            coreMember.managementService().registerTransactionEventListener(DEFAULT_DATABASE_NAME, listener);
        }

        cluster.coreTx( ( db, tx ) ->
        {
            tx.execute("MERGE (t:Test {uuid:'123XYZ'})");
            tx.commit();
        } );
    }
    @Test
    void mergeExistingNodeTest() throws Exception
    {
        // Do work here
        FederosTransactionEventListenerAdapter listener = new FederosTransactionEventListenerAdapter();
        for (CoreClusterMember coreMember : cluster.coreMembers()) {
            coreMember.managementService().registerTransactionEventListener(DEFAULT_DATABASE_NAME, listener);
        }

        cluster.coreTx( ( db, tx ) ->
        {
            tx.execute("CREATE (t:Test {uuid:'123XYZ'})");
            tx.commit();
        } );

        cluster.coreTx( ( db, tx ) ->
        {
            tx.execute("MERGE (t:Test {uuid:'123XYZ'})");
            tx.commit();
        } );
    }

    @Test
    void createNodesAndRelationshipTest() throws Exception
    {
        // Do work here
        FederosTransactionEventListenerAdapter listener = new FederosTransactionEventListenerAdapter();
        for (CoreClusterMember coreMember : cluster.coreMembers()) {
            coreMember.managementService().registerTransactionEventListener(DEFAULT_DATABASE_NAME, listener);
        }

        cluster.coreTx( ( db, tx ) ->
        {
            tx.execute("CREATE (t:Test {uuid:'123XYZ'})-[:CONNECTED_TO]->(t2:Test {uuid:'XYZ123'})");
            tx.commit();
        } );


    }

    @Test
    void deleteExistingNodeTest() throws Exception
    {
        // Do work here
        FederosTransactionEventListenerAdapter listener = new FederosTransactionEventListenerAdapter();
        for (CoreClusterMember coreMember : cluster.coreMembers()) {
            coreMember.managementService().registerTransactionEventListener(DEFAULT_DATABASE_NAME, listener);
        }

        cluster.coreTx( ( db, tx ) ->
        {
            tx.execute("CREATE (t:Test {uuid:'123XYZ'})");
            tx.commit();
        } );

        cluster.coreTx( ( db, tx ) ->
        {
            tx.execute("MATCH (t:Test {uuid:'123XYZ'}) DELETE t");
            tx.commit();
        } );
    }

    @Test
    void deleteDetachExistingNodeTest() throws Exception
    {
        // Do work here
        FederosTransactionEventListenerAdapter listener = new FederosTransactionEventListenerAdapter();
        for (CoreClusterMember coreMember : cluster.coreMembers()) {
            coreMember.managementService().registerTransactionEventListener(DEFAULT_DATABASE_NAME, listener);
        }

        cluster.coreTx( ( db, tx ) ->
        {
            tx.execute("CREATE (t:Test {uuid:'123XYZ'})-[:CONNECTED_TO]->(t2:Test {uuid:'XYZ123'})");
            tx.commit();
        } );

        cluster.coreTx( ( db, tx ) ->
        {
            tx.execute("MATCH (t:Test {uuid:'123XYZ'}) DETACH DELETE t");
            tx.commit();
        } );
    }

    @Test
    void deleteRelationshipTest() throws Exception
    {
        // Do work here
        FederosTransactionEventListenerAdapter listener = new FederosTransactionEventListenerAdapter();
        for (CoreClusterMember coreMember : cluster.coreMembers()) {
            coreMember.managementService().registerTransactionEventListener(DEFAULT_DATABASE_NAME, listener);
        }

        cluster.coreTx( ( db, tx ) ->
        {
            tx.execute("CREATE (t:Test {uuid:'123XYZ'})-[:CONNECTED_TO]->(t2:Test {uuid:'XYZ123'})");
            tx.commit();
        } );

        cluster.coreTx( ( db, tx ) ->
        {
            tx.execute("MATCH (t:Test {uuid:'123XYZ'})-[r:CONNECTED_TO]->(t2:Test {uuid:'XYZ123'}) DELETE r");
            tx.commit();
        } );
    }
}
