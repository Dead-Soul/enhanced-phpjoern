package outputModules.csv.exporters;

import databaseNodes.EdgeTypes;
import inherit.fake.FakeParentOfEdge;
import inherit.fake.FakeParentOfEdges;
import outputModules.common.Writer;
import outputModules.csv.common.FakeParentOfEdgeExporter;

public class CSVFakeParentOfExporter extends FakeParentOfEdgeExporter {

    @Override
    protected void addFakeParentOfEdge(FakeParentOfEdge fakeParentOfEdge) {

        long srcId = fakeParentOfEdge.getSrcId();
        long destId = fakeParentOfEdge.getDestId();

        Writer.appendRels(srcId, destId, EdgeTypes.PARENT_OF);
    }

    /**
     * Simple method that writes new created PARENT_OF edges to rels.csv
     */
    public void appendParentOfRels()
    {

        for (FakeParentOfEdge fakeParentOfEdge: FakeParentOfEdges.fakeParentOfEdges) {
            addFakeParentOfEdge(fakeParentOfEdge);
        }
    }
}
