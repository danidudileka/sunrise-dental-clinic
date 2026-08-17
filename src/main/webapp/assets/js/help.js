/**
 * Help section functionality
 */

document.addEventListener('DOMContentLoaded', function() {
    loadHelpTopics();
});

/**
 * Load help topics from API
 */
async function loadHelpTopics() {
    try {
        const response = await apiGet('/help/topics');

        if (response.status === 'SUCCESS') {
            displayHelpTopics(response.data);
        }

    } catch (error) {
        console.error('Error loading help topics:', error);
        // Show default help content
        displayDefaultHelp();
    }
}

/**
 * Display help topics
 */
function displayHelpTopics(topics) {
    const helpContainer = document.querySelector('.help-topics');

    if (!helpContainer || !topics || topics.length === 0) {
        return;
    }

    // Clear existing content
    helpContainer.innerHTML = '';

    // Create topic cards
    topics.forEach(topic => {
        const card = document.createElement('div');
        card.className = 'card';
        card.innerHTML = `
            <h2>${topic.title}</h2>
            <p>${topic.description}</p>
            <button onclick="loadTopicHelp('${topic.id}')" class="btn btn-secondary">
                View Instructions
            </button>
        `;
        helpContainer.appendChild(card);
    });
}

/**
 * Load specific topic help
 */
async function loadTopicHelp(topicId) {
    try {
        const response = await apiGet(`/help/${topicId}`);

        if (response.status === 'SUCCESS') {
            displayTopicHelp(response.data);
        }

    } catch (error) {
        console.error(`Error loading ${topicId} help:`, error);
    }
}

/**
 * Display topic help instructions
 */
function displayTopicHelp(helpData) {
    const helpContainer = document.querySelector('.help-topics');

    if (!helpContainer || !helpData) {
        return;
    }

    // Clear existing content
    helpContainer.innerHTML = '';

    // Create help card
    const card = document.createElement('div');
    card.className = 'card';

    let stepsHtml = '';
    if (helpData.steps && helpData.steps.length > 0) {
        stepsHtml = '<ol class="help-steps">';
        helpData.steps.forEach(step => {
            stepsHtml += `<li><strong>${step.title}:</strong> ${step.description}</li>`;
        });
        stepsHtml += '</ol>';
    }

    card.innerHTML = `
        <h2>${helpData.title}</h2>
        <p>${helpData.description}</p>
        ${stepsHtml}
        <button onclick="loadHelpTopics()" class="btn btn-secondary">Back to Topics</button>
    `;

    helpContainer.appendChild(card);
}

/**
 * Display default help content
 */
function displayDefaultHelp() {
    const helpContainer = document.querySelector('.help-topics');

    if (!helpContainer) {
        return;
    }

    helpContainer.innerHTML = `
        <div class="card">
            <h2>System Help</h2>
            <p>This system helps manage patient appointments and billing for Sunrise Dental Clinic.</p>
            <ol class="help-steps">
                <li>Use the navigation menu to access different features</li>
                <li>Register new appointments in the Appointments section</li>
                <li>Generate and process bills in the Billing section</li>
                <li>View reports and analytics in the Reports section</li>
                <li>Contact support if you need assistance</li>
            </ol>
        </div>
    `;
}