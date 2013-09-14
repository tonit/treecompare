package dogtooth.tree.internal;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dogtooth.tree.Tree;
import dogtooth.tree.TreeAlreadySealedException;
import dogtooth.tree.TreeBuilder;
import dogtooth.tree.TreeException;

public class InMemoryTreeBuilderImpl implements TreeBuilder {
	private final static Logger LOG = LoggerFactory.getLogger(InMemoryTreeBuilderImpl.class);
	private MessageDigest m_digest;
	private Tree m_hash;
	private boolean m_sealed = false;
	final private List<TreeBuilder> m_sub;
	private String m_selector;
	
	public InMemoryTreeBuilderImpl( ) {
        try {
           m_digest = MessageDigest.getInstance("SHA-1");
            m_sub = new ArrayList<TreeBuilder>();
        }catch (Exception e) {
            throw new TreeException("Problem creating collector",e);
        }
    }
	
	public InMemoryTreeBuilderImpl( final String selector ) {
		this();
		m_selector = selector;
	}
	
	/* (non-Javadoc)
	 * @see dogtooth.tree.internal.TreeBuilder#add(byte[])
	 */
	@Override
	synchronized public TreeBuilder add(byte[] bytes) {
		if (m_sealed) throw new TreeAlreadySealedException("No modification on a sealed tree!");
		m_digest.update(bytes);
		return this;
	}

	/* (non-Javadoc)
	 * @see dogtooth.tree.internal.TreeBuilder#seal()
	 */
	@Override
	synchronized public Tree seal() {
		if (!m_sealed) {
			// collect all sub elements..
			if (m_selector == null)  throw new TreeException("Sealing not possible due to missing selector.");
			List<Tree> subHashes = new ArrayList<Tree>(m_sub.size());
			for (TreeBuilder c : m_sub) {
				Tree subHash = c.seal();
				subHashes.add(subHash);
				add(subHash.getHashValue().getBytes());
			}
			m_hash = new InMemoryTreeImpl(m_selector,convertToHex(m_digest.digest()),subHashes.toArray(new Tree[subHashes.size()]));
			m_sealed = true;
			m_sub.clear();
			m_digest = null;
			m_selector = null;
			//System.gc();
		}
		return m_hash;
	}

	/* (non-Javadoc)
	 * @see dogtooth.tree.internal.TreeBuilder#childCollector()
	 */
	/**
	@Override
	synchronized public TreeBuilder childCollector() {
	    if (m_sealed) throw new TreeAlreadySealedException("No modification on a sealed tree!");
	    TreeBuilder c = new InMemoryTreeBuilderImpl();
        m_sub.add(c);
        return c;
	}
	**/
	
	/* (non-Javadoc)
	 * @see dogtooth.tree.internal.TreeBuilder#childCollector(java.lang.String)
	 */
	@Override
	synchronized public TreeBuilder childCollector( String selector) {
        if (m_sealed) throw new TreeAlreadySealedException("No modification on a sealed tree!");
        TreeBuilder c = new InMemoryTreeBuilderImpl(selector);
        m_sub.add(c);
        return c;
    }
	
	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	/* (non-Javadoc)
	 * @see dogtooth.tree.internal.TreeBuilder#setSelector(java.lang.String)
	 */
	@Override
	synchronized public TreeBuilder setSelector(String selector) {
	    if (m_sealed) throw new TreeAlreadySealedException("No modification on a sealed tree!");
	    m_selector = selector;
		return this;
	}
}