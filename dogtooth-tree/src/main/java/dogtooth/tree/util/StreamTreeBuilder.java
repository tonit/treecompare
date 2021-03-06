package dogtooth.tree.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dogtooth.tree.Selector;
import dogtooth.tree.Tree;
import dogtooth.tree.TreeBuilder;
import dogtooth.tree.TreeException;
import dogtooth.tree.annotated.Tag;

public class StreamTreeBuilder implements TreeBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger( StreamTreeBuilder.class );
    private long m_dataAmountRead = 0L;
    final private TreeBuilder m_delegate;
    
    public StreamTreeBuilder(final TreeBuilder delegate) {
        m_delegate = delegate;
    }
    
    public void add( final InputStream is )
        throws IOException {
        byte[] bytes = new byte[1024];
        int numRead = 0;
        while( (numRead = is.read( bytes )) >= 0 ) {
            m_delegate.add( Arrays.copyOf( bytes, numRead ) );
            m_dataAmountRead += numRead;
        }
    }

    public void add( final File f ) {
        try {
            InputStream is = new FileInputStream( f );
            try {
                add( is );
            }
            finally {
                if( is != null )
                    try {
                        is.close();
                    }
                    catch( IOException e ) {
                        LOG.warn("Problem closing file " + f.getAbsolutePath(),e);
                    }
            }
        }
        catch( IOException ioE ) {
            throw new TreeException( "Problem reading file " + f.getAbsolutePath() + " contents.", ioE );
        }
    }
    
    public long getDataRead() {
        return m_dataAmountRead;
    }
    
    public void reset() {
        m_dataAmountRead = 0L;
    }

    @Override
    public StreamTreeBuilder add( byte[] bytes ) {
        m_delegate.add(bytes);
        return this;
    }

    @Override
    public StreamTreeBuilder selector( Selector selector ) {
         m_delegate.selector( selector );
         return this;
    }

    @Override
    public StreamTreeBuilder branch( Selector selector ) {
        LOG.warn("Branching from StreamTreeBuilder is pretty unusually as it means you add raw data to an intermediate tree. ");
        return new StreamTreeBuilder( m_delegate.branch( selector ));
    }

    @Override
    public TreeBuilder tag( Tag tag ) {
        m_delegate.tag( tag );
        return this;
    }

    @Override
    public Tree seal() {
        return m_delegate.seal( );
    }
}
